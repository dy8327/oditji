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
    private final SearchContentAgeRatingService ageRatingService;
    private final TmdbProviderService providerService;
    private final SearchContentPolicyService contentPolicyService;

    public SearchContentCollectorService(
            SearchContentDiscoverService discoverService,
            SearchContentEnrichmentService enrichmentService,
            SearchContentManualOverrideService manualOverrideService,
            SearchContentAgeRatingService ageRatingService,
            TmdbProviderService providerService,
            SearchContentPolicyService contentPolicyService) {

        this.discoverService = discoverService;
        this.enrichmentService = enrichmentService;
        this.manualOverrideService = manualOverrideService;
        this.ageRatingService = ageRatingService;
        this.providerService = providerService;
        this.contentPolicyService = contentPolicyService;
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
        int normalizedBatchSize = Math.clamp(batchSize, 50, 2000);

        int movieTarget = Math.clamp(
                (int) Math.round(normalizedMaxSize * movieRatio),
                0,
                normalizedMaxSize
        );
        int tvTarget = normalizedMaxSize - movieTarget;

        manualOverrideService.reload();
        ageRatingService.reload();

        /*
         * 기존 스냅샷에서도 현재 노출 정책에 맞지 않는 제목을 먼저 제외합니다.
         * 이 단계에서 제거해야 외국어 제목 콘텐츠에 연령등급 재조회 API를
         * 불필요하게 호출하지 않습니다.
         */
        List<CachedContentVO> filteredPreviousSnapshot =
                filterByPolicy(previousSnapshot);

        /*
         * 기존 JSONL을 버리지 않고 그대로 사용한 상태에서
         * 수동 등급값을 먼저 반영한 뒤 "등급 정보 없음"만 전용 API로 재조회합니다.
         * 영화/TV 전체 상세 및 watch/providers를 다시 호출하지 않으므로
         * 기존 전체 보강보다 API 요청량이 크게 줄어듭니다.
         */
        ageRatingService.applyManualOverrides(filteredPreviousSnapshot);
        ageRatingService.recheckUnknownAgeRatings(filteredPreviousSnapshot);

        /*
         * 기존 JSONL에 이미 저장된 청소년 관람불가/등급 미확인 콘텐츠 중
         * 새 원본 등급 제외 정책을 아직 확인하지 않은 항목을 한 번 재검사합니다.
         * R18+, NC-17, TV-MA-S 계열이 확인되면 이 단계에서 목록에서 제거됩니다.
         */
        ageRatingService.recheckRestrictedAgeRatings(filteredPreviousSnapshot);

        TmdbProviderRegistry providerRegistry = providerService.loadRegistry();

        Map<String, CachedContentVO> previousMap =
                createContentMap(filteredPreviousSnapshot);

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

            /*
             * 등급 재조회 상태는 Discover 후보에 존재하지 않으므로
             * 기존 JSONL의 상태를 새 후보에 먼저 이어받습니다.
             */
            if (previous != null) {
                candidate.setAgeRatingRetryCount(
                        previous.getAgeRatingRetryCount()
                );
                candidate.setAgeRatingLastCheckedAt(
                        previous.getAgeRatingLastCheckedAt()
                );
            }

            if (enrichmentService.hasReusableDetail(previous)) {
                enrichmentService.copyReusableDetail(previous, candidate);
                manualOverrideService.apply(candidate);
                ageRatingService.applyManualOverride(candidate);
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
                        || contentPolicyService.shouldExcludeContent(content)
                        || content.getPlatformKeys() == null
                        || content.getPlatformKeys().isEmpty()) {
                    continue;
                }

                /*
                 * 신규 콘텐츠의 최초 등급 확인 시각을 남기고,
                 * 수동 등급값이 있으면 TMDB 결과보다 우선 적용합니다.
                 */
                ageRatingService.markLookupCompleted(content);
                ageRatingService.applyManualOverride(content);

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

        List<CachedContentVO> finalContents =
                sortAndLimit(
                        new ArrayList<CachedContentVO>(finalMap.values()),
                        normalizedMaxSize
                );

        /*
         * 추가 재조회까지 끝났지만 등급이 없는 콘텐츠만
         * 수동 확인용 JSON으로 별도 출력합니다.
         */
        ageRatingService.applyManualOverrides(finalContents);
        ageRatingService.writeMissingCandidates(finalContents);

        return finalContents;
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
                || contentPolicyService.shouldExcludeContent(content)
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

    /**
     * 기존 스냅샷에서 현재 공용 노출 정책을 통과한 콘텐츠만 복사합니다.
     */
    private List<CachedContentVO> filterByPolicy(
            List<CachedContentVO> source) {

        List<CachedContentVO> result =
                new ArrayList<CachedContentVO>();

        if (source == null) {
            return result;
        }

        for (CachedContentVO content : source) {
            if (content == null
                    || contentPolicyService.shouldExcludeContent(content)) {
                continue;
            }
            result.add(content);
        }

        return result;
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
                    || contentPolicyService.shouldExcludeContent(content)
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
                    || contentPolicyService.shouldExcludeContent(content)
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
