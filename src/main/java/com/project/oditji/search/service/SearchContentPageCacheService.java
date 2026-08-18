package com.project.oditji.search.service;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.project.oditji.common.util.DateTimeUtil;
import com.project.oditji.common.util.PlatformNameNormalizer;
import com.project.oditji.content.vo.ContentListPageVO;
import com.project.oditji.search.vo.CachedContentVO;
import com.project.oditji.search.vo.SearchResultPageVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.dao.TmdbDAO;
import com.project.oditji.tmdb.vo.OttPlatformVO;

@Service
public class SearchContentPageCacheService {

    private static final String MOVIE = "MOVIE";
    private static final String TV = "TV";

    private static final String CATEGORY_MOVIE = "MOVIE";
    private static final String CATEGORY_DRAMA = "DRAMA";
    private static final String CATEGORY_ANIMATION = "ANIMATION";
    private static final String CATEGORY_VARIETY = "VARIETY";
    private static final String CATEGORY_DOCUMENTARY = "DOCUMENTARY";

    private static final String GENRE_ANIMATION = "애니메이션";
    private static final String GENRE_DOCUMENTARY = "다큐멘터리";

    private static final String AGE_ALL = "전체 관람가";
    private static final String AGE_7 = "7세 이상 관람가";
    private static final String AGE_12 = "12세 이상 관람가";
    private static final String AGE_15 = "15세 이상 관람가";
    private static final String AGE_ADULT = "청소년 관람불가";
    private static final String AGE_UNKNOWN = "등급 정보 없음";

    private static final String SORT_RATING = "rating";
    private static final String SORT_LATEST = "latest";
    private static final String SORT_TITLE = "title";
    private static final String SORT_POPULAR = "popular";

    private static final int MAIN_NEW_UPCOMING_DAYS = 3;

    private static final String PLATFORM_NETFLIX = "netflix";
    private static final String PLATFORM_TVING = "tving";
    private static final String PLATFORM_WAVVE = "wavve";
    private static final String PLATFORM_DISNEY = "disney";
    private static final String PLATFORM_WATCHA = "watcha";
    private static final String PLATFORM_COUPANG = "coupang";

    private static final Map<String, String> LEGACY_PROVIDER_KEY_MAP = Map.of(
            "8", PLATFORM_NETFLIX,
            "1883", PLATFORM_TVING,
            "356", PLATFORM_WAVVE,
            "337", PLATFORM_DISNEY,
            "97", PLATFORM_WATCHA,
            "283", PLATFORM_COUPANG
    );

    private static final List<String> SUPPORTED_PLATFORM_ORDER = List.of(
            PLATFORM_NETFLIX,
            PLATFORM_TVING,
            PLATFORM_WAVVE,
            PLATFORM_DISNEY,
            PLATFORM_WATCHA,
            PLATFORM_COUPANG
    );

    private static final Set<String> SUPPORTED_PLATFORM_KEYS = Set.of(
            PLATFORM_NETFLIX,
            PLATFORM_TVING,
            PLATFORM_WAVVE,
            PLATFORM_DISNEY,
            PLATFORM_WATCHA,
            PLATFORM_COUPANG
    );

    private final SearchContentStore searchContentStore;
    private final TmdbDAO tmdbDAO;

    public SearchContentPageCacheService(
            SearchContentStore searchContentStore,
            TmdbDAO tmdbDAO) {

        this.searchContentStore =
                searchContentStore;

        this.tmdbDAO =
                tmdbDAO;
    }

    /**
     * 메인 배너 소개 슬라이드에 로고로 노출할 지원 OTT 목록입니다.
     *
     * TMDB 활성 플랫폼 목록 중 ODITJI가 실제로 지원하는
     * 플랫폼(SUPPORTED_PLATFORM_KEYS)만 골라, 항상 같은 순서
     * (넷플릭스 → 티빙 → 웨이브 → 디즈니+ → 왓챠 → 쿠팡플레이)로
     * 반환합니다. 활성 목록에 없는 플랫폼은 자연히 빠집니다.
     */
    public List<OttPlatformVO> getFeaturedPlatformList() {

        Map<String, OttPlatformVO> platformMap =
                createPlatformMap(
                        tmdbDAO.selectActivePlatformList()
                );

        List<OttPlatformVO> result =
                new ArrayList<OttPlatformVO>();

        for (String platformKey : SUPPORTED_PLATFORM_ORDER) {

            OttPlatformVO platform =
                    platformMap.get(platformKey);

            if (platform != null) {
                result.add(platform);
            }
        }

        return result;
    }

    /**
     * 메인 배너 소개 슬라이드에 노출할 전체 콘텐츠 수입니다.
     *
     * JSONL 공용 저장소에 실제로 적재된 건수를 그대로 반환합니다.
     * 화면에서는 이 값을 보기 좋게 반올림해서 "N,000+" 형태로 표시합니다.
     */
    public int getTotalContentCount() {
        return searchContentStore.size();
    }

    /**
     * 메인 우측 인기 콘텐츠를 JSONL 공용 저장소에서 조회합니다.
     */
    public List<SearchResultVO> getMainPopularContent(
            int limit) {

        return limitList(
                searchAll(
                        "",
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList()
                ),
                limit
        );
    }

    /**
     * 오늘의 콘텐츠를 JSONL 공용 저장소에서 조회합니다.
     *
     * 최근 30일 이내 공개작을 우선 사용하고,
     * 목록이 부족하면 최근 90일 이내 공개작,
     * 이후 전체 인기 콘텐츠 순으로 보충합니다.
     */
    public List<SearchResultVO> getMainTodayContent(
            int limit) {

        int normalizedLimit =
                Math.max(limit, 0);

        if (normalizedLimit == 0) {
            return new ArrayList<SearchResultVO>();
        }

        List<SearchResultVO> allContentList =
                searchAll(
                        "",
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList()
                );

        LocalDate today =
                LocalDate.now(DateTimeUtil.KOREA_ZONE);

        Map<String, SearchResultVO> selectedMap =
                new LinkedHashMap<String, SearchResultVO>();

        appendReleasedContent(
                selectedMap,
                allContentList,
                today.minusDays(30),
                today,
                normalizedLimit
        );

        appendReleasedContent(
                selectedMap,
                allContentList,
                today.minusDays(30),
                today,
                normalizedLimit
        );

        appendAllContent(
                selectedMap,
                allContentList,
                normalizedLimit
        );

        return new ArrayList<SearchResultVO>(
                selectedMap.values()
        );
    }

    /**
     * 신규 콘텐츠를 JSONL 공용 저장소에서 조회합니다.
     *
     * 공개일(releaseDate)이 오늘보다 3일을 초과해 미래인 콘텐츠는
     * 메인 신규 콘텐츠에서 제외합니다. 오늘 이후 3일 이내 공개작은
     * 그대로 노출하되 upcoming=true로 표시해 화면에서 "예정작"
     * 뱃지를 붙일 수 있게 합니다.
     *
     * 필터링 후 공개일 내림차순으로 정렬하고, 공개일이 같으면
     * 인기도, 평점 순으로 보조 정렬합니다.
     */
    public List<SearchResultVO> getMainNewContent(
            int limit) {

        List<SearchResultVO> allContentList =
                searchAll(
                        "",
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList()
                );

        applyUpcomingReleasePolicy(
                allContentList
        );

        allContentList.sort(
                createLatestComparator()
        );

        return limitList(
                allContentList,
                limit
        );
    }

    /**
     * 검색 결과와 신규 콘텐츠에서 공개 예정작 노출 범위를 통일합니다.
     *
     * 오늘 이후 3일 이내 콘텐츠는 upcoming=true로 표시하고,
     * 4일 이후 공개 예정 콘텐츠는 목록에서 제외합니다.
     * 공개일이 없거나 이미 공개된 콘텐츠는 그대로 유지합니다.
     */
    private void applyUpcomingReleasePolicy(
            List<SearchResultVO> contentList) {

        if (contentList == null
                || contentList.isEmpty()) {

            return;
        }

        LocalDate today =
                LocalDate.now(DateTimeUtil.KOREA_ZONE);

        LocalDate upcomingEndDate =
                today.plusDays(MAIN_NEW_UPCOMING_DAYS);

        contentList.removeIf(
                content -> {

                    LocalDate releaseDate =
                            parseReleaseDate(
                                    content == null
                                            ? null
                                            : content.getReleaseDate()
                            );

                    return releaseDate != null
                            && releaseDate.isAfter(upcomingEndDate);
                }
        );

        for (SearchResultVO content : contentList) {

            if (content == null) {
                continue;
            }

            LocalDate releaseDate =
                    parseReleaseDate(
                            content.getReleaseDate()
                    );

            /*
             * 4일 이후 예정작은 위 removeIf 단계에서 이미 제거되므로
             * 남은 항목은 오늘 이후 여부만 확인하면 됩니다.
             */
            content.setUpcoming(
                    releaseDate != null
                            && releaseDate.isAfter(today)
            );
        }
    }

    /**
     * 추천 콘텐츠를 JSONL 공용 저장소에서 조회합니다.
     *
     * 선택 OTT가 있으면 해당 OTT 콘텐츠만 사용하고,
     * 선택 OTT가 없으면 지원 OTT 전체를 사용합니다.
     *
     * 추천 우선순위:
     * 1. 인기도 높은 순
     * 2. 평점 높은 순
     */
    public List<SearchResultVO> getMainRecommendedContent(
            List<String> providerValues,
            int limit) {

        List<SearchResultVO> resultList =
                searchAll(
                        "",
                        Collections.emptyList(),
                        Collections.emptyList(),
                        providerValues
                );

        resultList.sort(
                Comparator
                        .comparing(
                                SearchResultVO::getPopularity,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()
                                )
                        )
                        .thenComparing(
                                SearchResultVO::getTmdbScore,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()
                                )
                        )
        );

        return limitList(
                resultList,
                limit
        );
    }

    private void appendReleasedContent(
            Map<String, SearchResultVO> selectedMap,
            List<SearchResultVO> sourceList,
            LocalDate startDate,
            LocalDate endDate,
            int limit) {

        if (selectedMap.size() >= limit
                || sourceList == null) {

            return;
        }

        for (int index = 0;
             index < sourceList.size()
                     && selectedMap.size() < limit;
             index++) {

            SearchResultVO content =
                    sourceList.get(index);

            LocalDate releaseDate =
                    parseReleaseDate(
                            content == null
                                    ? null
                                    : content.getReleaseDate()
                    );

            if (releaseDate != null
                    && !releaseDate.isBefore(startDate)
                    && !releaseDate.isAfter(endDate)) {

                putDistinctContent(
                        selectedMap,
                        content
                );
            }
        }
    }

    private void appendAllContent(
            Map<String, SearchResultVO> selectedMap,
            List<SearchResultVO> sourceList,
            int limit) {

        if (selectedMap.size() >= limit
                || sourceList == null) {

            return;
        }

        for (SearchResultVO content : sourceList) {

            if (selectedMap.size() >= limit) {
                break;
            }

            putDistinctContent(
                    selectedMap,
                    content
            );
        }
    }

    private void putDistinctContent(
            Map<String, SearchResultVO> selectedMap,
            SearchResultVO content) {

        if (content == null
                || content.getTmdbId() == null
                || content.getContentType() == null) {

            return;
        }

        String key =
                content.getContentType()
                + ":"
                + content.getTmdbId();

        selectedMap.putIfAbsent(
                key,
                content
        );
    }

    private LocalDate parseReleaseDate(
            String releaseDate) {

        if (releaseDate == null
                || releaseDate.isBlank()) {

            return null;
        }

        try {

            return LocalDate.parse(
                    releaseDate.trim()
            );

        } catch (Exception e) {

            return null;
        }
    }

    private List<SearchResultVO> limitList(
            List<SearchResultVO> sourceList,
            int limit) {

        if (sourceList == null
                || sourceList.isEmpty()
                || limit <= 0) {

            return new ArrayList<SearchResultVO>();
        }

        if (sourceList.size() <= limit) {
            return new ArrayList<SearchResultVO>(
                    sourceList
            );
        }

        return new ArrayList<SearchResultVO>(
                sourceList.subList(
                        0,
                        limit
                )
        );
    }


    /**
     * "출시 알림 캘린더"에 노출할 콘텐츠를 특정 연·월 기준으로 조회합니다.
     *
     * releaseDate(개봉·공개일)가 해당 연·월에 속하는 콘텐츠를
     * 공개일 오름차순으로 정렬해 반환합니다.
     */
    public List<SearchResultVO> getReleaseCalendarContent(
            int year,
            int month) {

        List<SearchResultVO> allContentList =
                searchAll(
                        "",
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList()
                );

        List<SearchResultVO> monthContentList =
                new ArrayList<SearchResultVO>();

        for (SearchResultVO content : allContentList) {

            /* searchAll은 null 콘텐츠를 결과 목록에 넣지 않습니다. */
            LocalDate releaseDate =
                    parseReleaseDate(
                            content.getReleaseDate()
                    );

            if (releaseDate != null
                    && releaseDate.getYear() == year
                    && releaseDate.getMonthValue() == month) {

                monthContentList.add(content);
            }
        }

        monthContentList.sort(
                Comparator.comparing(
                        content -> parseReleaseDate(
                                content.getReleaseDate()
                        )
                )
        );

        return monthContentList;
    }

    /**
     * 메인 "최근 본 콘텐츠" 슬라이더용 조회입니다.
     *
     * CONTENT_VIEW_HISTORY 기준으로 이미 최근 조회순 정렬되어 넘어온
     * ContentVO 목록을 받아, JSONL 공용 저장소에서 TMDB ID + 콘텐츠 유형이
     * 일치하는 SearchResultVO로 변환합니다. (관련 콘텐츠 매칭과 동일한 방식)
     *
     * JSONL 저장소에서 내려간(더 이상 존재하지 않는) 콘텐츠는
     * 자연히 결과에서 제외됩니다.
     */
    public List<SearchResultVO> getMainRecentlyViewedContent(
            List<com.project.oditji.content.vo.ContentVO> orderedContentList) {

        if (orderedContentList == null
                || orderedContentList.isEmpty()) {

            return new ArrayList<SearchResultVO>();
        }

        List<SearchResultVO> allContentList =
                searchAll(
                        "",
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList()
                );

        Map<String, SearchResultVO> candidateByTmdbKey =
                new HashMap<String, SearchResultVO>();

        for (SearchResultVO candidate : allContentList) {

            candidateByTmdbKey.put(
                    buildTmdbKey(
                            candidate.getTmdbId(),
                            candidate.getContentType()
                    ),
                    candidate
            );
        }

        List<SearchResultVO> result =
                new ArrayList<SearchResultVO>();

        for (com.project.oditji.content.vo.ContentVO viewedContent
                : orderedContentList) {

            SearchResultVO matched =
                    candidateByTmdbKey.get(
                            buildTmdbKey(
                                    viewedContent.getTmdbId(),
                                    viewedContent.getContentType()
                            )
                    );

            if (matched != null) {
                result.add(matched);
            }
        }

        return result;
    }

    /**
     * TMDB ID + 콘텐츠 유형으로 콘텐츠를 식별하는 매칭 키를 만듭니다.
     */
    private String buildTmdbKey(
            Long tmdbId,
            String contentType) {

        return tmdbId
                + "_"
                + normalizeRelatedValue(contentType);
    }

    /**
     * 콘텐츠 상세 페이지의 관련 콘텐츠를 JSONL 공용 저장소에서 조회합니다.
     *
     * 추천 우선순위:
     * 1. 동일 분류(영화, 드라마, 애니메이션, 예능, 다큐멘터리)
     * 2. 주 장르 일치
     * 3. 겹치는 전체 장르 수
     * 4. 같은 감독
     * 5. 같은 출연진
     * 6. TMDB 평점
     * 7. TMDB 인기도
     *
     * 현재 상세 콘텐츠 자체는 TMDB ID와 콘텐츠 유형으로 제외합니다.
     */
    public List<SearchResultVO> getRelatedContentList(
            com.project.oditji.content.vo.ContentVO currentContent,
            int limit) {

        if (currentContent == null || limit <= 0) {
            return new ArrayList<SearchResultVO>();
        }

        List<SearchResultVO> candidateList =
                searchAll(
                        "",
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList()
                );

        final String currentCategory =
                resolveRelatedCategory(
                        currentContent.getContentType(),
                        currentContent.getGenreText()
                );

        final List<String> currentGenreList =
                splitRelatedValues(
                        currentContent.getGenreText()
                );

        final Set<String> currentGenres =
                new HashSet<String>(
                        currentGenreList
                );

        final String currentMainGenre =
                resolveRelatedMainGenre(
                        currentGenreList
                );

        final Set<String> currentDirectors =
                new HashSet<String>(
                        splitRelatedValues(
                                currentContent.getDirector()
                        )
                );

        final Set<String> currentCast =
                new HashSet<String>(
                        splitRelatedValues(
                                currentContent.getCastNames()
                        )
                );

        candidateList.removeIf(candidate -> {

            /* searchAll에서 null 후보는 이미 제외됩니다. */
            boolean sameContent =
                    currentContent.getTmdbId() != null
                    && currentContent.getTmdbId().equals(
                            candidate.getTmdbId()
                    )
                    && normalizeRelatedValue(
                            currentContent.getContentType()
                    ).equals(
                            normalizeRelatedValue(
                                    candidate.getContentType()
                            )
                    );

            if (sameContent) {
                return true;
            }

            String candidateCategory =
                    resolveRelatedCategory(
                            candidate.getContentType(),
                            candidate.getGenreText()
                    );

            return !currentCategory.equals(
                    candidateCategory
            );
        });

        candidateList.sort(
                Comparator
                        .comparingInt(
                                (SearchResultVO candidate) ->
                                        calculateRelatedScore(
                                                candidate,
                                                currentGenres,
                                                currentMainGenre,
                                                currentDirectors,
                                                currentCast
                                        )
                        )
                        .reversed()
                        .thenComparing(
                                SearchResultVO::getTmdbScore,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()
                                )
                        )
                        .thenComparing(
                                SearchResultVO::getPopularity,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()
                                )
                        )
        );

        List<SearchResultVO> relatedContentList =
                limitList(
                        candidateList,
                        limit
                );

        /*
         * 정렬에 사용한 동일한 비교 기준으로 추천 이유를 생성합니다.
         * 점수 기준과 화면 문구가 서로 어긋나지 않도록
         * 최종 반환 대상에만 이유를 설정합니다.
         */
        for (SearchResultVO relatedContent : relatedContentList) {

            /*
             * 후보 목록 생성 단계에서 null을 제거하고 있지만,
             * limitList는 일반 List를 복사하므로 정적 분석에서는 null 원소가 가능합니다.
             * 추후 목록 가공 방식이 변경되더라도 NPE가 발생하지 않도록 방어합니다.
             */
            if (relatedContent == null) {
                continue;
            }

            relatedContent.setRecommendationReason(
                    createRelatedRecommendationReason(
                            currentContent,
                            relatedContent,
                            currentGenres,
                            currentMainGenre,
                            currentDirectors,
                            currentCast
                    )
            );

            /*
             * 추천 문구의 핵심 근거를 별도 유형으로 내려줍니다.
             * JSP는 이 값을 CSS 클래스에 연결하여 추천 이유별로
             * 서로 다른 강조 색상을 적용합니다.
             */
            relatedContent.setRecommendationReasonType(
                    resolveRelatedRecommendationReasonType(
                            currentContent,
                            relatedContent,
                            currentMainGenre
                    )
            );
        }

        return relatedContentList;
    }

    /**
     * 관련 콘텐츠 한 건에 표시할 추천 이유를 생성합니다.
     *
     * 추천 점수 우선순위와 동일하게 주 장르를 먼저 설명하고,
     * 추가 장르·감독·출연진 중 의미 있는 일치 정보를 덧붙입니다.
     * 카드가 복잡해지지 않도록 최대 두 가지 핵심 근거만 사용합니다.
     */
    private String createRelatedRecommendationReason(
            com.project.oditji.content.vo.ContentVO currentContent,
            SearchResultVO candidate,
            Set<String> currentGenres,
            String currentMainGenre,
            Set<String> currentDirectors,
            Set<String> currentCast) {

        if (candidate == null) {
            return "비슷한 콘텐츠로 추천했어요.";
        }

        List<String> candidateGenreList =
                splitRelatedValues(
                        candidate.getGenreText()
                );

        Set<String> candidateGenres =
                new HashSet<String>(
                        candidateGenreList
                );

        String candidateMainGenre =
                resolveRelatedMainGenre(
                        candidateGenreList
                );

        /*
         * 일치값 검색 메서드는 기본적으로 빈 문자열을 반환하지만,
         * 정적 분석과 예외적인 반환값까지 고려해 null을 빈 문자열로 보정합니다.
         */
        String matchedGenre =
                safeText(
                        findFirstOriginalMatchedValue(
                                currentContent.getGenreText(),
                                candidate.getGenreText()
                        )
                );

        String matchedDirector =
                safeText(
                        findFirstOriginalMatchedValue(
                                currentContent.getDirector(),
                                candidate.getDirector()
                        )
                );

        String matchedCast =
                safeText(
                        findFirstOriginalMatchedValue(
                                currentContent.getCastNames(),
                                candidate.getCastNames()
                        )
                );

        StringBuilder reason =
                new StringBuilder();

        boolean mainGenreMatched =
                !currentMainGenre.isEmpty()
                && currentMainGenre.equals(
                        candidateMainGenre
                );

        if (mainGenreMatched) {

            /*
             * 원래 장르 표기를 찾지 못한 경우에도 isEmpty() 호출이 안전하도록
             * null을 빈 문자열로 변환합니다.
             */
            String originalMainGenre =
                    safeText(
                            findOriginalValue(
                                    currentContent.getGenreText(),
                                    currentMainGenre
                            )
                    );

            reason.append("같은 ")
                    .append(
                            originalMainGenre.isEmpty()
                                    ? currentMainGenre
                                    : originalMainGenre
                    )
                    .append(" 장르의 작품이에요.");

        } else if (!matchedGenre.isEmpty()) {

            reason.append(matchedGenre)
                    .append(" 장르가 비슷한 작품이에요.");

        } else if (!candidateGenres.isEmpty()
                && !currentGenres.isEmpty()) {

            reason.append("비슷한 장르 구성을 가진 작품이에요.");

        } else {

            reason.append("같은 콘텐츠 분류에서 추천한 작품이에요.");
        }

        if (!matchedDirector.isEmpty()
                && currentDirectors.contains(
                        normalizeRelatedValue(
                                matchedDirector
                        )
                )) {

            reason.append(" ")
                    .append(matchedDirector)
                    .append(" 감독의 작품이에요.");

        } else if (!matchedCast.isEmpty()
                && currentCast.contains(
                        normalizeRelatedValue(
                                matchedCast
                        )
                )) {

            reason.append(" 출연진 " )
                    .append(matchedCast)
                    .append("이(가) 함께해요.");
        }

        return reason.toString();
    }

    /**
     * 추천 이유에서 가장 구체적인 핵심 근거를 유형으로 반환합니다.
     *
     * 한 카드에서 장르와 인물 정보가 함께 일치할 수 있으므로
     * 감독 > 출연진 > 주 장르 > 일반 장르 > 콘텐츠 분류 순서로
     * 대표 유형을 결정합니다.
     */
    private String resolveRelatedRecommendationReasonType(
            com.project.oditji.content.vo.ContentVO currentContent,
            SearchResultVO candidate,
            String currentMainGenre) {

        if (currentContent == null || candidate == null) {
            return "CATEGORY";
        }

        /*
         * 추천 이유 유형 판별에서도 일치값을 안전한 문자열로 보정해
         * SonarQube가 지적한 NullPointerException 가능성을 제거합니다.
         */
        String matchedDirector =
                safeText(
                        findFirstOriginalMatchedValue(
                                currentContent.getDirector(),
                                candidate.getDirector()
                        )
                );

        if (!matchedDirector.isEmpty()) {
            return "DIRECTOR";
        }

        String matchedCast =
                safeText(
                        findFirstOriginalMatchedValue(
                                currentContent.getCastNames(),
                                candidate.getCastNames()
                        )
                );

        if (!matchedCast.isEmpty()) {
            return "CAST";
        }

        List<String> candidateGenreList =
                splitRelatedValues(
                        candidate.getGenreText()
                );

        String candidateMainGenre =
                resolveRelatedMainGenre(
                        candidateGenreList
                );

        if (currentMainGenre != null
                && !currentMainGenre.isEmpty()
                && currentMainGenre.equals(
                        candidateMainGenre
                )) {

            return "MAIN_GENRE";
        }

        String matchedGenre =
                safeText(
                        findFirstOriginalMatchedValue(
                                currentContent.getGenreText(),
                                candidate.getGenreText()
                        )
                );

        if (!matchedGenre.isEmpty()) {
            return "GENRE";
        }

        return "CATEGORY";
    }

    /**
     * 쉼표로 구분된 두 문자열에서 처음 일치하는 값을 찾습니다.
     * 비교는 소문자로 정규화하지만, 화면에는 후보 콘텐츠의 원래 표기를 반환합니다.
     */
    private String findFirstOriginalMatchedValue(
            String currentValue,
            String candidateValue) {

        Set<String> currentNormalizedValues =
                new HashSet<String>(
                        splitRelatedValues(
                                currentValue
                        )
                );

        if (currentNormalizedValues.isEmpty()
                || candidateValue == null
                || candidateValue.isBlank()) {

            return "";
        }

        String[] candidateTokens =
                candidateValue.split(",");

        for (String candidateToken : candidateTokens) {

            /* String.split 결과 원소는 null이 될 수 없습니다. */
            String originalValue =
                    candidateToken.trim();

            String normalizedValue =
                    normalizeRelatedValue(
                            originalValue
                    );

            if (!normalizedValue.isEmpty()
                    && currentNormalizedValues.contains(
                            normalizedValue
                    )) {

                return originalValue;
            }
        }

        return "";
    }

    /**
     * 정규화된 비교값에 대응하는 원래 표기를 반환합니다.
     */
    private String findOriginalValue(
            String originalValues,
            String normalizedTarget) {

        if (originalValues == null
                || originalValues.isBlank()
                || normalizedTarget == null
                || normalizedTarget.isBlank()) {

            return "";
        }

        String[] tokens =
                originalValues.split(",");

        for (String token : tokens) {

            /* String.split 결과 원소는 null이 될 수 없습니다. */
            String originalValue =
                    token.trim();

            if (normalizeRelatedValue(
                    originalValue
            ).equals(normalizedTarget)) {

                return originalValue;
            }
        }

        return "";
    }

    /**
     * 관련 콘텐츠 한 건의 유사도 점수를 계산합니다.
     */
    private int calculateRelatedScore(
            SearchResultVO candidate,
            Set<String> currentGenres,
            String currentMainGenre,
            Set<String> currentDirectors,
            Set<String> currentCast) {

        int score = 0;

        List<String> candidateGenreList =
                splitRelatedValues(
                        candidate.getGenreText()
                );

        Set<String> candidateGenres =
                new HashSet<String>(
                        candidateGenreList
                );

        String candidateMainGenre =
                resolveRelatedMainGenre(
                        candidateGenreList
                );

        /*
         * 사용자가 요청한 우선순위대로
         * 주 장르 일치를 가장 크게 반영합니다.
         */
        if (!currentMainGenre.isEmpty()
                && currentMainGenre.equals(
                        candidateMainGenre
                )) {

            score += 1000;
        }

        for (String genre : candidateGenres) {

            if (currentGenres.contains(genre)) {
                score += 50;
            }
        }

        Set<String> candidateDirectors =
                new HashSet<String>(
                        splitRelatedValues(
                                candidate.getDirector()
                        )
                );

        for (String director : candidateDirectors) {

            if (currentDirectors.contains(director)) {
                score += 30;
            }
        }

        Set<String> candidateCast =
                new HashSet<String>(
                        splitRelatedValues(
                                candidate.getCastNames()
                        )
                );

        for (String castName : candidateCast) {

            if (currentCast.contains(castName)) {
                score += 10;
            }
        }

        return score;
    }

    /**
     * 콘텐츠를 영화, 드라마, 애니메이션, 예능, 다큐멘터리로 구분합니다.
     */
    private String resolveRelatedCategory(
            String contentType,
            String genreText) {

        String normalizedType =
                normalizeRelatedValue(
                        contentType
                );

        List<String> genres =
                splitRelatedValues(
                        genreText
                );

        if (genres.contains(GENRE_ANIMATION)) {
            return CATEGORY_ANIMATION;
        }

        if (genres.contains(GENRE_DOCUMENTARY)) {
            return CATEGORY_DOCUMENTARY;
        }

        if (genres.contains("리얼리티")
                || genres.contains("토크")) {

            return CATEGORY_VARIETY;
        }

        if ("movie".equals(normalizedType)) {
            return CATEGORY_MOVIE;
        }

        return CATEGORY_DRAMA;
    }

    /**
     * 장르 목록에서 일반 분류용 장르를 제외하고
     * 첫 번째 핵심 장르를 주 장르로 사용합니다.
     */
    private String resolveRelatedMainGenre(
            List<String> genres) {

        if (genres == null || genres.isEmpty()) {
            return "";
        }

        Set<String> excludedGenres =
                new HashSet<String>();

        Collections.addAll(
                excludedGenres,
                "드라마",
                GENRE_ANIMATION,
                GENRE_DOCUMENTARY,
                "리얼리티",
                "토크",
                "연속극",
                "키즈",
                "tv영화",
                "뉴스"
        );

        for (String genre : genres) {

            String comparisonValue =
                    genre.replace(" ", "");

            if (!excludedGenres.contains(
                    comparisonValue
            )) {

                return genre;
            }
        }

        return genres.get(0);
    }

    /**
     * 쉼표로 구분된 장르, 감독, 출연진 값을
     * 비교 가능한 소문자 목록으로 변환합니다.
     */
    private List<String> splitRelatedValues(
            String value) {

        List<String> result =
                new ArrayList<String>();

        if (value == null || value.isBlank()) {
            return result;
        }

        String[] tokens =
                value.split(",");

        for (String token : tokens) {

            String normalized =
                    normalizeRelatedValue(
                            token
                    );

            if (!normalized.isEmpty()
                    && !result.contains(normalized)) {

                result.add(normalized);
            }
        }

        return result;
    }

    private String normalizeRelatedValue(
            String value) {

        return value == null
                ? ""
                : value.trim()
                        .toLowerCase(Locale.ROOT);
    }


    /**
     * 상단 영화·시리즈, 인기, 신규 탭의 콘텐츠 목록을
     * JSONL 공용 저장소에서 조회합니다.
     *
     * type은 전체·인기·신규 목록 범위를 결정하고,
     * sort는 인기순·평점순·최신순·가나다순 정렬을 결정합니다.
     * 신규 탭은 최근 30일 공개작과 오늘 이후 3일 이내 예정작만 사용합니다.
     */
    public ContentListPageVO getContentListPage(
            String type,
            String sort,
            int displayPage,
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds,
            List<String> ageRatings) {

        final int pageSize = 20;

        String normalizedType =
                normalizeContentListType(type);

        String normalizedSort =
                normalizeContentListSort(
                        sort,
                        normalizedType
                );

        int normalizedPage =
                Math.max(displayPage, 1);

        List<SearchResultVO> filteredList =
                searchAll(
                        "",
                        contentCategories,
                        genreCodes,
                        providerIds,
                        ageRatings
                );

        if ("new".equals(normalizedType)) {

            /*
             * 신규 탭도 메인과 동일하게 오늘 이후 3일 이내 예정작은
             * 표시하고, 4일 이후 예정작은 제외합니다.
             * 과거 콘텐츠는 최근 30일 공개작만 유지합니다.
             */
            LocalDate today =
                    LocalDate.now(DateTimeUtil.KOREA_ZONE);

            LocalDate startDate =
                    today.minusDays(30);

            applyUpcomingReleasePolicy(
                    filteredList
            );

            filteredList.removeIf(
                    content -> {

                        /* searchAll은 null 콘텐츠를 결과 목록에 넣지 않습니다. */
                        LocalDate releaseDate =
                                parseReleaseDate(
                                        content.getReleaseDate()
                                );

                        return releaseDate == null
                                || releaseDate.isBefore(startDate);
                    }
            );
        }

        sortContentList(
                filteredList,
                normalizedType,
                normalizedSort
        );

        int totalResults =
                filteredList.size();

        int totalPages =
                totalResults == 0
                        ? 0
                        : (totalResults
                                + pageSize
                                - 1)
                                / pageSize;

        if (totalPages > 0
                && normalizedPage > totalPages) {

            normalizedPage =
                    totalPages;
        }

        int startIndex =
                (normalizedPage - 1)
                        * pageSize;

        int endIndex =
                Math.min(
                        startIndex + pageSize,
                        totalResults
                );

        List<SearchResultVO> pageContentList =
                new ArrayList<SearchResultVO>();

        /* normalizedPage는 항상 1 이상이므로 startIndex도 음수가 될 수 없습니다. */
        if (startIndex < totalResults) {

            pageContentList.addAll(
                    filteredList.subList(
                            startIndex,
                            endIndex
                    )
            );
        }

        ContentListPageVO pageVO =
                new ContentListPageVO();

        pageVO.setContentList(
                pageContentList
        );

        pageVO.setCurrentPage(
                normalizedPage
        );

        pageVO.setTotalPages(
                totalPages
        );

        pageVO.setTotalResults(
                totalResults
        );

        return pageVO;
    }

    /**
     * 콘텐츠 목록 하단의 가로형 추천 영역을 JSONL에서 조회합니다.
     *
     * 현재 선택된 콘텐츠 종류, 장르, OTT 필터를 반영한 뒤
     * 인기도 내림차순, 평점 내림차순으로 정렬합니다.
     */
    public List<SearchResultVO> getContentRecommendedList(
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds,
            List<String> ageRatings,
            int limit) {

        List<SearchResultVO> recommendedList =
                searchAll(
                        "",
                        contentCategories,
                        genreCodes,
                        providerIds,
                        ageRatings
                );

        recommendedList.sort(
                Comparator
                        .comparing(
                                SearchResultVO::getPopularity,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()
                                )
                        )
                        .thenComparing(
                                SearchResultVO::getTmdbScore,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()
                                )
                        )
        );

        return limitList(
                recommendedList,
                limit
        );
    }

    /**
     * 사용자가 선택한 정렬 기준을 전체 필터 결과에 적용한 뒤
     * 페이징하도록 정렬합니다.
     */
    private void sortContentList(
            List<SearchResultVO> contentList,
            String type,
            String sort) {

        String normalizedSort =
                normalizeContentListSort(
                        sort,
                        type
                );

        contentList.sort(
                createContentListComparator(
                        normalizedSort
                )
        );
    }

    private Comparator<SearchResultVO> createContentListComparator(
            String normalizedSort) {

        return switch (normalizedSort) {
            case SORT_RATING -> createRatingComparator();
            case SORT_LATEST -> createLatestComparator();
            case SORT_TITLE -> createTitleComparator();
            default -> createPopularComparator();
        };
    }

    private Comparator<SearchResultVO> createRatingComparator() {
        return Comparator
                .comparing(
                        SearchResultVO::getTmdbScore,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                )
                .thenComparing(
                        SearchResultVO::getPopularity,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                );
    }

    private Comparator<SearchResultVO> createLatestComparator() {
        return Comparator
                .comparing(
                        SearchResultVO::getReleaseDate,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                )
                .thenComparing(
                        SearchResultVO::getPopularity,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                )
                .thenComparing(
                        SearchResultVO::getTmdbScore,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                );
    }

    private Comparator<SearchResultVO> createTitleComparator() {
        return Comparator
                .comparing(
                        SearchResultVO::getTitle,
                        String.CASE_INSENSITIVE_ORDER
                )
                .thenComparing(
                        SearchResultVO::getPopularity,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                );
    }

    private Comparator<SearchResultVO> createPopularComparator() {
        return Comparator
                .comparing(
                        SearchResultVO::getPopularity,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                )
                .thenComparing(
                        SearchResultVO::getTmdbScore,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                );
    }

    /**
     * 허용하지 않은 정렬값이 들어오면 탭에 맞는 기본값을 사용합니다.
     */
    private String normalizeContentListSort(
            String sort,
            String type) {

        String defaultSort =
                "new".equals(type)
                        ? SORT_LATEST
                        : SORT_POPULAR;

        if (sort == null
                || sort.isBlank()) {

            return defaultSort;
        }

        String normalized =
                sort.trim()
                        .toLowerCase(Locale.ROOT);

        if (SORT_POPULAR.equals(normalized)
                || SORT_RATING.equals(normalized)
                || SORT_LATEST.equals(normalized)
                || SORT_TITLE.equals(normalized)) {

            return normalized;
        }

        return defaultSort;
    }

    /**
     * 잘못된 탭 값이 들어오면 영화·시리즈(all)로 처리합니다.
     */
    private String normalizeContentListType(
            String type) {

        String normalized =
                type == null
                        ? "all"
                        : type.trim()
                                .toLowerCase(
                                        Locale.ROOT
                                );

        if (SORT_POPULAR.equals(normalized)
                || "new".equals(normalized)) {

            return normalized;
        }

        return "all";
    }

    public SearchResultPageVO getContentPage(
            String keyword,
            int displayPage,
            int pageSize,
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds) {

        return getContentPage(
                keyword,
                displayPage,
                pageSize,
                contentCategories,
                genreCodes,
                providerIds,
                Collections.emptyList()
        );
    }

    /**
     * 검색결과 화면의 관람등급 조건까지 포함하여 콘텐츠를 조회합니다.
     */
    public SearchResultPageVO getContentPage(
            String keyword,
            int displayPage,
            int pageSize,
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds,
            List<String> ageRatings) {

        int normalizedPage =
                Math.max(displayPage, 1);

        int normalizedPageSize =
                Math.clamp(
                        pageSize,
                        1,
                        100
                );

        List<SearchResultVO> filtered =
                searchAll(
                        keyword,
                        contentCategories,
                        genreCodes,
                        providerIds,
                        ageRatings
                );

        /*
         * 검색 결과도 메인 신규 콘텐츠와 동일한 공개 예정 기준을 사용합니다.
         * 3일 이내 예정작은 유지하고 upcoming=true로 표시하며,
         * 4일 이후 예정작은 검색 결과 건수와 페이징에서 함께 제외합니다.
         */
        applyUpcomingReleasePolicy(
                filtered
        );

        int totalResults =
                filtered.size();

        int totalPages =
                totalResults == 0
                        ? 0
                        : (totalResults
                                + normalizedPageSize
                                - 1)
                                / normalizedPageSize;

        if (totalPages > 0
                && normalizedPage > totalPages) {

            normalizedPage = totalPages;
        }

        int startIndex =
                (normalizedPage - 1)
                        * normalizedPageSize;

        int endIndex =
                Math.min(
                        startIndex
                                + normalizedPageSize,
                        totalResults
                );

        List<SearchResultVO> pageResult =
                new ArrayList<SearchResultVO>();

        /* normalizedPage와 normalizedPageSize는 모두 1 이상입니다. */
        if (startIndex < totalResults) {

            pageResult.addAll(
                    filtered.subList(
                            startIndex,
                            endIndex
                    )
            );
        }

        SearchResultPageVO pageVO =
                new SearchResultPageVO();

        pageVO.setPage(normalizedPage);
        pageVO.setResultList(pageResult);
        pageVO.setTotalPages(totalPages);
        pageVO.setTotalResults(totalResults);

        return pageVO;
    }

    public List<SearchResultVO> getFirstPagePreview(
            String keyword,
            int previewSize,
            int contentPageSize,
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds,
            List<String> ageRatings) {

        if (previewSize <= 0) {
            return new ArrayList<SearchResultVO>();
        }

        SearchResultPageVO pageVO =
                getContentPage(
                        keyword,
                        1,
                        Math.max(
                                previewSize,
                                contentPageSize
                        ),
                        contentCategories,
                        genreCodes,
                        providerIds,
                        ageRatings
                );

        List<SearchResultVO> resultList =
                pageVO.getResultList();

        /* getContentPage는 항상 비-null 결과 목록을 설정합니다. */
        if (resultList.isEmpty()) {

            return new ArrayList<SearchResultVO>();
        }

        int endIndex =
                Math.min(
                        previewSize,
                        resultList.size()
                );

        return new ArrayList<SearchResultVO>(
                resultList.subList(
                        0,
                        endIndex
                )
        );
    }

    /**
     * 헤더 검색창 자동완성 드롭다운용 콘텐츠/배우 미리보기입니다.
     *
     * 필터 없이 검색어만으로 매칭하며,
     * getFirstPagePreview와 동일한 인기순 정렬·매칭 정보(matchType 등)를 그대로 사용합니다.
     */
    public List<SearchResultVO> getAutocompletePreview(
            String keyword,
            int limit) {

        if (keyword == null
                || keyword.trim().isEmpty()
                || limit <= 0) {

            return new ArrayList<SearchResultVO>();
        }

        return getFirstPagePreview(
                keyword,
                limit,
                limit,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    private List<SearchResultVO> searchAll(
            String keyword,
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds) {

        return searchAll(
                keyword,
                contentCategories,
                genreCodes,
                providerIds,
                Collections.emptyList()
        );
    }

    private List<SearchResultVO> searchAll(
            String keyword,
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds,
            List<String> ageRatings) {

        String normalizedKeyword =
                normalizeSearchText(keyword);

        List<String> normalizedCategories =
                normalizeUpperCaseList(
                        contentCategories
                );

        List<String> normalizedGenres =
                normalizeUpperCaseList(
                        genreCodes
                );

        List<String> normalizedAgeRatings =
                normalizeAgeRatingList(
                        ageRatings
                );

        Set<String> selectedPlatformKeys =
                providerIdsToKeys(
                        providerIds
                );

        Map<String, OttPlatformVO> platformMap =
                createPlatformMap(
                        tmdbDAO.selectActivePlatformList()
                );

        List<SearchResultVO> result =
                new ArrayList<SearchResultVO>();

        for (CachedContentVO content
                : searchContentStore.getAll()) {

            if (matchesSearchFilters(
                    content,
                    normalizedKeyword,
                    normalizedCategories,
                    normalizedGenres,
                    normalizedAgeRatings,
                    selectedPlatformKeys
            )) {

                result.add(
                        toSearchResultVO(
                                content,
                                platformMap,
                                normalizedKeyword
                        )
                );
            }
        }

        result.sort(
                Comparator
                        .comparing(
                                SearchResultVO::getPopularity,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()
                                )
                        )
                        .thenComparing(
                                SearchResultVO::getTmdbScore,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()
                                )
                        )
        );

        return result;
    }


    private boolean matchesSearchFilters(
            CachedContentVO content,
            String normalizedKeyword,
            List<String> normalizedCategories,
            List<String> normalizedGenres,
            List<String> normalizedAgeRatings,
            Set<String> selectedPlatformKeys) {

        return content != null
                && content.getTmdbId() != null
                && content.getContentType() != null
                && matchesKeyword(
                        content,
                        normalizedKeyword
                )
                && matchesContentCategories(
                        content,
                        normalizedCategories
                )
                && matchesGenreCodes(
                        content,
                        normalizedGenres
                )
                && matchesAgeRatings(
                        content,
                        normalizedAgeRatings
                )
                && matchesProviders(
                        content,
                        selectedPlatformKeys
                );
    }

    private boolean matchesKeyword(
            CachedContentVO content,
            String normalizedKeyword) {

        if (normalizedKeyword.isEmpty()) {
            return true;
        }

        String searchText =
                content.getSearchText();

        if (searchText == null
                || searchText.isBlank()) {

            searchText =
                    normalizeSearchText(
                            safeText(
                                    content.getTitle()
                            )
                                    + " "
                                    + safeText(
                                            content.getOriginalTitle()
                                    )
                                    + " "
                                    + safeText(
                                            content.getDirector()
                                    )
                                    + " "
                                    + safeText(
                                            content.getCastNames()
                                    )
                    );
        }

        return searchText.contains(
                normalizedKeyword
        );
    }

    private boolean matchesContentCategories(
            CachedContentVO content,
            List<String> categories) {

        if (categories.isEmpty()) {
            return true;
        }

        for (String category
                : categories) {

            if (matchesContentCategory(
                    content,
                    category
            )) {

                return true;
            }
        }

        return false;
    }

    private boolean matchesContentCategory(
                CachedContentVO content,
                String category) {

        String contentType =
                safeText(
                        content.getContentType()
                ).toUpperCase(Locale.ROOT);

        String genreText =
                safeText(
                        content.getGenreText()
                );

        boolean animation =
                genreText.contains(GENRE_ANIMATION);

        boolean documentary =
                genreText.contains(GENRE_DOCUMENTARY);

        boolean variety =
                genreText.contains("리얼리티")
                        || genreText.contains("토크");

        if (CATEGORY_MOVIE.equals(category)) {

                return MOVIE.equals(contentType)
                        && !animation
                        && !documentary;
        }

        if (CATEGORY_DRAMA.equals(category)) {

                return TV.equals(contentType)
                        && genreText.contains("드라마")
                        && !animation
                        && !documentary
                        && !variety;
        }

        if (CATEGORY_ANIMATION.equals(category)) {
                return animation;
        }

        if (CATEGORY_VARIETY.equals(category)) {

                return TV.equals(contentType)
                        && variety;
        }
        return CATEGORY_DOCUMENTARY.equals(category)
                && documentary;
        }

    private boolean matchesGenreCodes(
            CachedContentVO content,
            List<String> genreCodes) {

        if (genreCodes.isEmpty()) {
            return true;
        }

        String genreText =
                safeText(
                        content.getGenreText()
                );

        for (String genreCode
                : genreCodes) {

            String genreName =
                    displayGenreName(
                            genreCode
                    );

            if (genreText.contains(
                    genreName
            )) {

                return true;
            }

            /*
             * ACTION의 "액션·모험"과 SCI_FI/FANTASY의 "SF·판타지"는
             * 위 표준 장르명 contains 검사에서 이미 일치합니다.
             */
            if ("ROMANCE".equals(genreCode)
                    && genreText.contains(
                            "연속극"
                    )) {

                return true;
            }
        }

        return false;
    }


    /**
     * 선택된 관람등급 중 하나와 콘텐츠의 관람등급이 일치하는지 확인합니다.
     * 서로 다른 표기와 공백 차이는 한국식 표준 등급으로 정규화합니다.
     */
    private boolean matchesAgeRatings(
            CachedContentVO content,
            List<String> ageRatings) {

        if (ageRatings.isEmpty()) {
            return true;
        }

        String contentAgeRating =
                normalizeAgeRating(
                        content == null
                                ? null
                                : content.getAgeRating()
                );

        return ageRatings.contains(contentAgeRating);
    }

    private List<String> normalizeAgeRatingList(
            List<String> sourceList) {

        List<String> result =
                new ArrayList<String>();

        if (sourceList == null) {
            return result;
        }

        for (String value : sourceList) {

            if (value == null
                    || value.isBlank()) {

                continue;
            }

            String normalized =
                    normalizeAgeRating(value);

            if (!normalized.isEmpty()
                    && !result.contains(normalized)) {

                result.add(normalized);
            }
        }

        return result;
    }

    private String normalizeAgeRating(
            String value) {

        if (value == null
                || value.isBlank()) {

            return AGE_UNKNOWN;
        }

        String normalized =
                Normalizer.normalize(
                        value,
                        Normalizer.Form.NFKC
                )
                        .replaceAll("\\s+", "")
                        .toLowerCase(Locale.ROOT);

        if (normalized.contains("등급정보없음")
                || normalized.contains("notrated")
                || normalized.contains("unrated")
                || "nr".equals(normalized)) {

            return AGE_UNKNOWN;
        }

        if (normalized.contains("청소년관람불가")
                || normalized.contains("19세")
                || normalized.contains("18세")) {

            return AGE_ADULT;
        }

        if (normalized.contains("15세")) {
            return AGE_15;
        }

        if (normalized.contains("12세")) {
            return AGE_12;
        }

        if (normalized.contains("7세")) {
            return AGE_7;
        }

        if (normalized.contains("전체")) {
            return AGE_ALL;
        }

        return value.trim();
    }

    private boolean matchesProviders(
            CachedContentVO content,
            Set<String> selectedPlatformKeys) {

        if (selectedPlatformKeys.isEmpty()) {
            return !content
                    .getPlatformKeys()
                    .isEmpty();
        }

        for (String platformKey
                : content.getPlatformKeys()) {

            if (selectedPlatformKeys.contains(
                    platformKey
            )) {

                return true;
            }
        }

        return false;
    }

    private SearchResultVO toSearchResultVO(
            CachedContentVO content,
            Map<String, OttPlatformVO> platformMap,
            String normalizedKeyword) {

        SearchResultVO result =
                new SearchResultVO();

        result.setTmdbId(
                content.getTmdbId()
        );

        result.setContentType(
                content.getContentType()
        );

        result.setTitle(
                content.getTitle()
        );

        result.setOriginalTitle(
                content.getOriginalTitle()
        );

        result.setPosterPath(
                content.getPosterPath()
        );

        result.setReleaseDate(
                content.getReleaseDate()
        );

        result.setLastAirDate(
                content.getLastAirDate()
        );

        result.setGenreText(
                content.getGenreText()
        );

        result.setAgeRating(
                content.getAgeRating()
        );

        result.setTmdbScore(
                content.getTmdbScore()
        );

        result.setPopularity(
                content.getPopularity()
        );

        result.setEpisodeCount(
                content.getEpisodeCount()
        );

        result.setDirector(
                content.getDirector()
        );

        result.setCastNames(
                content.getCastNames()
        );

        result.setPlatformList(
                createPlatformList(
                        content.getPlatformKeys(),
                        platformMap
                )
        );

        applyMatchInformation(
                result,
                content,
                normalizedKeyword
        );

        return result;
    }

    private void applyMatchInformation(
            SearchResultVO result,
            CachedContentVO content,
            String normalizedKeyword) {

        result.setMatchType("TITLE");

        if (normalizedKeyword.isEmpty()) {
            return;
        }

        String normalizedTitle =
                normalizeSearchText(
                        safeText(
                                content.getTitle()
                        )
                                + " "
                                + safeText(
                                        content.getOriginalTitle()
                                )
                );

        if (normalizedTitle.contains(
                normalizedKeyword
        )) {

            return;
        }

        String normalizedDirector =
                normalizeSearchText(
                        content.getDirector()
                );

        if (normalizedDirector.contains(
                normalizedKeyword
        )) {

            result.setMatchType("PERSON");

            result.setMatchedPersonName(
                    content.getDirector()
            );

            result.setMatchedPersonRole(
                    "감독"
            );

            return;
        }

        String normalizedCast =
                normalizeSearchText(
                        content.getCastNames()
                );

        if (normalizedCast.contains(
                normalizedKeyword
        )) {

            result.setMatchType("PERSON");

            result.setMatchedPersonName(
                    content.getCastNames()
            );

            result.setMatchedPersonRole(
                    "배우"
            );
        }
    }

    private List<OttPlatformVO> createPlatformList(
            List<String> platformKeys,
            Map<String, OttPlatformVO> platformMap) {

        List<OttPlatformVO> result =
                new ArrayList<OttPlatformVO>();

        if (platformKeys == null) {
            return result;
        }

        for (String platformKey
                : platformKeys) {

            OttPlatformVO platform =
                    platformMap.get(
                            platformKey
                    );

            if (platform != null) {
                result.add(platform);
            }
        }

        return result;
    }

    private Map<String, OttPlatformVO> createPlatformMap(
            List<OttPlatformVO> platformList) {

        Map<String, OttPlatformVO> result =
                new LinkedHashMap<String, OttPlatformVO>();

        if (platformList == null) {
            return result;
        }

        for (OttPlatformVO platform
                : platformList) {

            if (platform == null
                    || platform.getPlatformName() == null) {

                continue;
            }

            String key =
                    PlatformNameNormalizer.toKey(
                            platform.getPlatformName()
                    );

            if (!key.isEmpty()) {
                result.put(key, platform);
            }
        }

        return result;
    }

    /**
     * 사이드바에서 전달된 OTT 값을 내부 플랫폼 키로 변환합니다.
     *
     * 신규 화면에서는 netflix, tving 등의 내부 키를 직접 사용하고,
     * 기존 북마크나 URL 호환을 위해 과거 숫자 값도 함께 지원합니다.
     *
     * 숫자 283은 TMDB에서 Crunchyroll이지만,
     * 과거 ODITJI 화면에서 쿠팡플레이 값으로 사용했기 때문에
     * 기존 URL 호환 목적으로만 coupang으로 처리합니다.
     */
    private Set<String> providerIdsToKeys(
            List<String> providerIds) {

        Set<String> result =
                new HashSet<String>();

        if (providerIds == null) {
            return result;
        }

        for (String providerId : providerIds) {
            String platformKey =
                    resolveProviderKey(providerId);

            if (!platformKey.isEmpty()) {
                result.add(platformKey);
            }
        }

        return result;
    }

    private String resolveProviderKey(
            String providerId) {

        if (providerId == null
                || providerId.isBlank()) {

            return "";
        }

        String rawValue =
                providerId.trim();

        String legacyPlatformKey =
                LEGACY_PROVIDER_KEY_MAP.get(rawValue);

        if (legacyPlatformKey != null) {
            return legacyPlatformKey;
        }

        String platformKey =
                PlatformNameNormalizer.toKey(rawValue);

        return SUPPORTED_PLATFORM_KEYS.contains(platformKey)
                ? platformKey
                : "";
    }

    private List<String> normalizeUpperCaseList(
            List<String> sourceList) {

        List<String> result =
                new ArrayList<String>();

        if (sourceList == null) {
            return result;
        }

        for (String value
                : sourceList) {

            if (value == null) {
                continue;
            }

            String normalized =
                    value.trim()
                            .toUpperCase(
                                    Locale.ROOT
                            );

            if (!normalized.isEmpty()
                    && !result.contains(normalized)) {

                result.add(normalized);
            }
        }

        return result;
    }

    private String normalizeSearchText(
            String value) {

        if (value == null) {
            return "";
        }

        String normalized =
                Normalizer.normalize(
                        value,
                        Normalizer.Form.NFKC
                );

        return normalized
                .toLowerCase(Locale.ROOT)
                .replaceAll(
                        "[^\\p{L}\\p{N}]",
                        ""
                );
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private String displayGenreName(
            String code) {

        Map<String, String> names =
                new HashMap<String, String>();

        names.put("ACTION", "액션");
        names.put("ADVENTURE", "모험");
        names.put(CATEGORY_ANIMATION, GENRE_ANIMATION);
        names.put("COMEDY", "코미디");
        names.put("CRIME", "범죄");
        names.put(CATEGORY_DOCUMENTARY, GENRE_DOCUMENTARY);
        names.put(CATEGORY_DRAMA, "드라마");
        names.put("FAMILY", "가족");
        names.put("FANTASY", "판타지");
        names.put("HISTORY", "역사");
        names.put("HORROR", "공포");
        names.put("MUSIC", "음악");
        names.put("MYSTERY", "미스터리");
        names.put("ROMANCE", "로맨스");
        names.put("SCI_FI", "SF");
        names.put("THRILLER", "스릴러");
        names.put("WAR", "전쟁");
        names.put("WESTERN", "서부");
        names.put("KIDS", "키즈");
        names.put("NEWS", "뉴스");
        names.put("REALITY", "리얼리티");
        names.put("SOAP", "연속극");
        names.put("TALK", "토크");

        return names.getOrDefault(
                code,
                code
        );
    }
}