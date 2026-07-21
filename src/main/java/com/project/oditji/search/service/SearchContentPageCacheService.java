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
                LocalDate.now();

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
                today.minusDays(90),
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

        for (SearchResultVO content : sourceList) {

            if (selectedMap.size() >= limit) {
                break;
            }

            LocalDate releaseDate =
                    parseReleaseDate(
                            content == null
                                    ? null
                                    : content.getReleaseDate()
                    );

            if (releaseDate == null
                    || releaseDate.isBefore(startDate)
                    || releaseDate.isAfter(endDate)) {

                continue;
            }

            putDistinctContent(
                    selectedMap,
                    content
            );
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

            if (candidate == null) {
                return true;
            }

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

        return limitList(
                candidateList,
                limit
        );
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

        if (genres.contains("애니메이션")) {
            return CATEGORY_ANIMATION;
        }

        if (genres.contains("다큐멘터리")) {
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
                "애니메이션",
                "다큐멘터리",
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
     * type별 정렬 기준:
     * - all: 평점 내림차순, 인기도 내림차순
     * - popular: 인기도 내림차순, 평점 내림차순
     * - new: 최근 90일 이내 공개작을 공개일 내림차순으로 정렬
     */
    public ContentListPageVO getContentListPage(
            String type,
            int displayPage,
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds) {

        final int pageSize = 20;

        String normalizedType =
                normalizeContentListType(type);

        int normalizedPage =
                Math.max(displayPage, 1);

        List<SearchResultVO> filteredList =
                searchAll(
                        "",
                        contentCategories,
                        genreCodes,
                        providerIds
                );

        if ("new".equals(normalizedType)) {

            /*
             * 신규 탭은 오늘보다 미래인 콘텐츠를 제외하고,
             * 최근 90일 이내 공개된 콘텐츠만 표시합니다.
             */
            LocalDate today =
                    LocalDate.now();

            LocalDate startDate =
                    today.minusDays(90);

            filteredList.removeIf(
                    content -> {

                        LocalDate releaseDate =
                                parseReleaseDate(
                                        content == null
                                                ? null
                                                : content.getReleaseDate()
                                );

                        return releaseDate == null
                                || releaseDate.isBefore(startDate)
                                || releaseDate.isAfter(today);
                    }
            );
        }

        sortContentListByType(
                filteredList,
                normalizedType
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

        if (startIndex >= 0
                && startIndex < totalResults) {

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
     * 콘텐츠 목록 우측의 추천 캐러셀을 JSONL에서 조회합니다.
     *
     * 현재 선택된 콘텐츠 종류, 장르, OTT 필터를 반영한 뒤
     * 인기도 내림차순, 평점 내림차순으로 정렬합니다.
     */
    public List<SearchResultVO> getContentRecommendedList(
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds,
            int limit) {

        List<SearchResultVO> recommendedList =
                searchAll(
                        "",
                        contentCategories,
                        genreCodes,
                        providerIds
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
     * 콘텐츠 목록 탭에 따라 정렬 기준을 적용합니다.
     */
    private void sortContentListByType(
            List<SearchResultVO> contentList,
            String type) {

        Comparator<SearchResultVO> comparator;

        if ("new".equals(type)) {

            comparator =
                    Comparator
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

        } else if ("all".equals(type)) {

            comparator =
                    Comparator
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

        } else {

            comparator =
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
                            );
        }

        contentList.sort(
                comparator
        );
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

        if ("popular".equals(normalized)
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

        int normalizedPage =
                Math.max(displayPage, 1);

        int normalizedPageSize =
                Math.max(
                        1,
                        Math.min(
                                pageSize,
                                100
                        )
                );

        List<SearchResultVO> filtered =
                searchAll(
                        keyword,
                        contentCategories,
                        genreCodes,
                        providerIds
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

        if (startIndex >= 0
                && startIndex < totalResults) {

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
            List<String> providerIds) {

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
                        providerIds
                );

        List<SearchResultVO> resultList =
                pageVO.getResultList();

        if (resultList == null
                || resultList.isEmpty()) {

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

    private List<SearchResultVO> searchAll(
            String keyword,
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds) {

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

            if (content == null
                    || content.getTmdbId() == null
                    || content.getContentType() == null) {

                continue;
            }

            if (!matchesKeyword(
                    content,
                    normalizedKeyword
            )) {

                continue;
            }

            if (!matchesContentCategories(
                    content,
                    normalizedCategories
            )) {

                continue;
            }

            if (!matchesGenreCodes(
                    content,
                    normalizedGenres
            )) {

                continue;
            }

            if (!matchesProviders(
                    content,
                    selectedPlatformKeys
            )) {

                continue;
            }

            result.add(
                    toSearchResultVO(
                            content,
                            platformMap,
                            normalizedKeyword
                    )
            );
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
                genreText.contains("애니메이션");

        boolean documentary =
                genreText.contains("다큐멘터리");

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

            if ("ACTION".equals(genreCode)
                    && genreText.contains(
                            "액션·모험"
                    )) {

                return true;
            }

            if (("SCI_FI".equals(genreCode)
                    || "FANTASY".equals(genreCode))
                    && genreText.contains(
                            "SF·판타지"
                    )) {

                return true;
            }

            if ("ROMANCE".equals(genreCode)
                    && genreText.contains(
                            "연속극"
                    )) {

                return true;
            }
        }

        return false;
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
                    normalizePlatformName(
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

            if (providerId == null
                    || providerId.isBlank()) {

                continue;
            }

            String rawValue =
                    providerId.trim();

            String normalizedValue =
                    rawValue.toLowerCase(
                            Locale.ROOT
                    );

            if ("8".equals(normalizedValue)) {
                result.add("netflix");
                continue;
            }

            if ("1883".equals(normalizedValue)) {
                result.add("tving");
                continue;
            }

            if ("356".equals(normalizedValue)) {
                result.add("wavve");
                continue;
            }

            if ("337".equals(normalizedValue)) {
                result.add("disney");
                continue;
            }

            if ("97".equals(normalizedValue)) {
                result.add("watcha");
                continue;
            }

            if ("283".equals(normalizedValue)) {
                result.add("coupang");
                continue;
            }

            String platformKey =
                    normalizePlatformName(
                            rawValue
                    );

            if ("netflix".equals(platformKey)
                    || "tving".equals(platformKey)
                    || "wavve".equals(platformKey)
                    || "disney".equals(platformKey)
                    || "watcha".equals(platformKey)
                    || "coupang".equals(platformKey)) {

                result.add(platformKey);
            }
        }

        return result;
    }

    private String normalizePlatformName(
            String name) {

        if (name == null) {
            return "";
        }

        String normalized =
                name.toLowerCase(Locale.ROOT)
                        .replaceAll(
                                "[^a-z0-9]",
                                ""
                        );

        if (normalized.contains("netflix")) {
            return "netflix";
        }

        if (normalized.contains("tving")) {
            return "tving";
        }

        if (normalized.contains("wavve")) {
            return "wavve";
        }

        if (normalized.contains("disney")) {
            return "disney";
        }

        if (normalized.contains("watcha")) {
            return "watcha";
        }

        if (normalized.contains("coupang")) {
            return "coupang";
        }

        return normalized;
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
        names.put("ANIMATION", "애니메이션");
        names.put("COMEDY", "코미디");
        names.put("CRIME", "범죄");
        names.put("DOCUMENTARY", "다큐멘터리");
        names.put("DRAMA", "드라마");
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