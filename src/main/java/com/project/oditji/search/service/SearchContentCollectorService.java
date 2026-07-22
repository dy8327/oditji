package com.project.oditji.search.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.project.oditji.search.vo.CachedContentVO;

/**
 * 검색 콘텐츠 전체 수집 순서를 조정하는 서비스입니다.
 *
 * 실제 TMDB 통신, 후보 수집, 상세 보강, 제공처 매핑과 수동 OTT 처리는
 * 각각의 전용 서비스에 위임합니다. 이 클래스는 이전 스냅샷 재사용,
 * 배치 처리, 체크포인트 전달과 최종 목록 구성만 담당합니다.
 */
@Service
public class SearchContentCollectorService {

    @Value("${search.content-cache.max-size:50000}")
    private int maxSize;

    @Value("${search.content-cache.movie-ratio:0.5}")
    private double movieRatio;

    @Value("${search.content-cache.batch-size:500}")
    private int batchSize;

    private final SearchContentDiscoverService discoverService;
    private final SearchContentEnrichmentService enrichmentService;
    private final SearchContentManualOverrideService manualOverrideService;
    private final TmdbProviderService providerService;

    public SearchContentCollectorService(
            SearchContentDiscoverService discoverService,
            SearchContentEnrichmentService enrichmentService,
            SearchContentManualOverrideService manualOverrideService,
            TmdbProviderService providerService) {

        this.discoverService = discoverService;
        this.enrichmentService = enrichmentService;
        this.manualOverrideService = manualOverrideService;
        this.providerService = providerService;
    }

    /**
     * 중간 저장 콜백을 사용하지 않는 기존 호출부 호환 메서드입니다.
     */
    public List<CachedContentVO> collect(
            List<CachedContentVO> previousSnapshot) {

        return collect(previousSnapshot, ignored -> {
            /* 기존 호출부 호환용 빈 콜백입니다. */
        });
    }

    /**
     * 검색 콘텐츠를 수집하고 배치가 완료될 때마다 체크포인트를 전달합니다.
     *
     * @param previousSnapshot 이전 JSONL 스냅샷
     * @param checkpointConsumer 중간 저장 콜백
     * @return 이번 수집 기준 최종 콘텐츠 목록
     */
    public List<CachedContentVO> collect(
            List<CachedContentVO> previousSnapshot,
            Consumer<List<CachedContentVO>> checkpointConsumer) {

        int normalizedMaxSize = Math.max(100, maxSize);
        int normalizedBatchSize = Math.max(50, Math.min(batchSize, 2000));

        int movieTarget = Math.max(
                0,
                Math.min(
                        normalizedMaxSize,
                        (int) Math.round(normalizedMaxSize * movieRatio)
                )
        );
        int tvTarget = normalizedMaxSize - movieTarget;

        manualOverrideService.reload();
        TmdbProviderRegistry providerRegistry = providerService.loadRegistry();

        Map<String, CachedContentVO> previousMap =
                createContentMap(previousSnapshot);

        List<CachedContentVO> candidates = new ArrayList<CachedContentVO>();
        candidates.addAll(discoverService.collectMovieCandidates(
                movieTarget,
                providerRegistry.getMovieProviderIds()
        ));
        candidates.addAll(discoverService.collectTvCandidates(
                tvTarget,
                providerRegistry.getTvProviderIds()
        ));
        candidates.addAll(discoverService.collectSupplementCandidates());

        /*
         * 수동 등록 후보는 provider 또는 Discover 결과에 없어도 반드시
         * 상세 보강 대상으로 남도록 일반 후보보다 우선 정렬합니다.
         */
        candidates.addAll(manualOverrideService.createCandidates());
        candidates = removeDuplicate(candidates);
        candidates.sort(createCandidateComparator());

        if (candidates.size() > normalizedMaxSize) {
            candidates = new ArrayList<CachedContentVO>(
                    candidates.subList(0, normalizedMaxSize)
            );
        }

        Map<String, CachedContentVO> checkpointMap =
                new LinkedHashMap<String, CachedContentVO>(previousMap);
        Map<String, CachedContentVO> finalMap =
                new LinkedHashMap<String, CachedContentVO>();
        List<CachedContentVO> pendingCandidates =
                new ArrayList<CachedContentVO>();

        for (CachedContentVO candidate : candidates) {
            CachedContentVO previous = previousMap.get(
                    candidate.createContentKey()
            );

            if (enrichmentService.hasReusableDetail(previous)) {
                enrichmentService.copyReusableDetail(previous, candidate);
                manualOverrideService.apply(candidate);
                candidate.setSearchText(
                        enrichmentService.createSearchText(candidate)
                );

                checkpointMap.put(candidate.createContentKey(), candidate);
                finalMap.put(candidate.createContentKey(), candidate);
            } else {
                pendingCandidates.add(candidate);
            }
        }

        if (!finalMap.isEmpty()) {
            publishCheckpoint(
                    checkpointMap,
                    normalizedMaxSize,
                    checkpointConsumer
            );
        }

        for (int startIndex = 0;
             startIndex < pendingCandidates.size();
             startIndex += normalizedBatchSize) {

            int endIndex = Math.min(
                    startIndex + normalizedBatchSize,
                    pendingCandidates.size()
            );

            List<CachedContentVO> batch = new ArrayList<CachedContentVO>(
                    pendingCandidates.subList(startIndex, endIndex)
            );

            List<CachedContentVO> enrichedBatch =
                    enrichmentService.enrichBatch(batch, providerRegistry);

            for (CachedContentVO content : enrichedBatch) {
                if (content == null
                        || content.getPlatformKeys() == null
                        || content.getPlatformKeys().isEmpty()) {
                    continue;
                }

                String key = content.createContentKey();
                checkpointMap.put(key, content);
                finalMap.put(key, content);
            }

            publishCheckpoint(
                    checkpointMap,
                    normalizedMaxSize,
                    checkpointConsumer
            );
        }

        return sortAndLimit(
                new ArrayList<CachedContentVO>(finalMap.values()),
                normalizedMaxSize
        );
    }

    /**
     * 수동 등록 콘텐츠를 최우선으로 두고 나머지를 인기도 내림차순으로 정렬합니다.
     */
    private Comparator<CachedContentVO> createCandidateComparator() {
        return Comparator
                .comparing(
                        (CachedContentVO content) ->
                                manualOverrideService.contains(content) ? 0 : 1
                )
                .thenComparing(
                        CachedContentVO::getPopularity,
                        Comparator.nullsLast(Comparator.reverseOrder())
                );
    }

    private void publishCheckpoint(
            Map<String, CachedContentVO> checkpointMap,
            int limit,
            Consumer<List<CachedContentVO>> checkpointConsumer) {

        List<CachedContentVO> checkpoint = sortAndLimit(
                new ArrayList<CachedContentVO>(checkpointMap.values()),
                limit
        );
        checkpointConsumer.accept(checkpoint);
    }

    private List<CachedContentVO> sortAndLimit(
            List<CachedContentVO> source,
            int limit) {

        source.removeIf(content -> content == null
                || content.getTmdbId() == null
                || content.getContentType() == null
                || content.getPlatformKeys() == null
                || content.getPlatformKeys().isEmpty());

        source.sort(createCandidateComparator());

        if (source.size() <= limit) {
            return source;
        }

        return new ArrayList<CachedContentVO>(source.subList(0, limit));
    }

    private Map<String, CachedContentVO> createContentMap(
            List<CachedContentVO> source) {

        Map<String, CachedContentVO> result =
                new LinkedHashMap<String, CachedContentVO>();

        if (source == null) {
            return result;
        }

        for (CachedContentVO content : source) {
            if (content == null
                    || content.getTmdbId() == null
                    || content.getContentType() == null) {
                continue;
            }
            result.put(content.createContentKey(), content);
        }

        return result;
    }

    private List<CachedContentVO> removeDuplicate(
            List<CachedContentVO> source) {

        Map<String, CachedContentVO> result =
                new LinkedHashMap<String, CachedContentVO>();

        for (CachedContentVO content : source) {
            if (content == null
                    || content.getTmdbId() == null
                    || content.getContentType() == null) {
                continue;
            }

            String key = content.createContentKey();
            CachedContentVO existing = result.get(key);

            /*
             * 수동 후보는 ID와 유형만 가지므로 기존 Discover 후보의 제목과
             * 인기도를 지우지 않도록 더 많은 정보가 있는 후보를 유지합니다.
             */
            if (existing == null || hasMoreBasicInformation(content, existing)) {
                result.put(key, content);
            }
        }

        return new ArrayList<CachedContentVO>(result.values());
    }

    private boolean hasMoreBasicInformation(
            CachedContentVO candidate,
            CachedContentVO existing) {

        return basicInformationScore(candidate)
                > basicInformationScore(existing);
    }

    private int basicInformationScore(CachedContentVO content) {
        int score = 0;
        if (content.getTitle() != null && !content.getTitle().isBlank()) {
            score++;
        }
        if (content.getOriginalTitle() != null
                && !content.getOriginalTitle().isBlank()) {
            score++;
        }
        if (content.getPosterPath() != null
                && !content.getPosterPath().isBlank()) {
            score++;
        }
        if (content.getReleaseDate() != null
                && !content.getReleaseDate().isBlank()) {
            score++;
        }
        if (content.getGenreText() != null
                && !content.getGenreText().isBlank()) {
            score++;
        }
        if (content.getPopularity() != null) {
            score++;
        }
        return score;
    }
}