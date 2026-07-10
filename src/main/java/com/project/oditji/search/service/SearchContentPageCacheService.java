package com.project.oditji.search.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.project.oditji.search.vo.SearchResultPageVO;
import com.project.oditji.search.vo.SearchResultVO;

@Service
public class SearchContentPageCacheService {

    /*
     * 검색 결과 누적 캐시 유지 시간
     */
    private static final long CACHE_TTL_MILLIS =
            Duration.ofMinutes(10).toMillis();

    /*
     * 검색 조건 캐시 최대 개수
     */
    private static final int MAX_CACHE_SIZE = 200;

    /*
     * TMDB에서 접근할 수 있는 최대 페이지
     */
    private static final int MAX_TMDB_PAGE = 500;

    private final SearchService searchService;

    private final Map<String, SearchCacheEntry> searchCache =
            new ConcurrentHashMap<String, SearchCacheEntry>();

    public SearchContentPageCacheService(
            SearchService searchService) {

        this.searchService = searchService;
    }

    /**
     * 화면에 표시할 콘텐츠 페이지를 반환한다.
     *
     * 동일 검색 조건으로 다음 페이지를 요청하면
     * 기존에 수집된 결과를 다시 사용하고,
     * 부족한 경우 마지막으로 조회한 TMDB 페이지 다음부터 이어서 조회한다.
     */
    public SearchResultPageVO getContentPage(
            String keyword,
            int displayPage,
            int pageSize,
            List<String> contentTypes,
            List<String> genreCodes,
            List<String> providerIds) {

        int normalizedPage =
                normalizePage(displayPage);

        int normalizedPageSize =
                normalizePageSize(pageSize);

        String normalizedKeyword =
                normalizeKeyword(keyword);

        List<String> normalizedContentTypes =
                normalizeAndSortList(contentTypes);

        List<String> normalizedGenreCodes =
                normalizeAndSortList(genreCodes);

        List<String> normalizedProviderIds =
                normalizeAndSortList(providerIds);

        String cacheKey =
                createCacheKey(
                        normalizedKeyword,
                        normalizedContentTypes,
                        normalizedGenreCodes,
                        normalizedProviderIds
                );

        SearchCacheEntry cacheEntry =
                getOrCreateCacheEntry(cacheKey);

        int startIndex =
                (normalizedPage - 1)
                        * normalizedPageSize;

        /*
         * 다음 페이지가 있는지 판단하기 위해
         * 현재 페이지 끝보다 한 건 더 수집한다.
         */
        int requiredResultCount =
                startIndex
                        + normalizedPageSize
                        + 1;

        synchronized (cacheEntry) {

            /*
             * 캐시가 만료됐으면 현재 엔트리를 초기화한다.
             */
            if (cacheEntry.isExpired()) {

                cacheEntry.reset();
            }

            collectUntilRequiredCount(
                    cacheEntry,
                    requiredResultCount,
                    normalizedKeyword,
                    normalizedContentTypes,
                    normalizedGenreCodes,
                    normalizedProviderIds
            );

            return createDisplayPage(
                    cacheEntry,
                    normalizedPage,
                    normalizedPageSize
            );
        }
    }

    /**
     * 전체 탭에서 사용할 콘텐츠 1페이지 상위 결과를 반환한다.
     *
     * 이미 콘텐츠 페이지를 조회했다면 같은 누적 캐시에서 바로 꺼낸다.
     */
    public List<SearchResultVO> getFirstPagePreview(
            String keyword,
            int previewSize,
            int contentPageSize,
            List<String> contentTypes,
            List<String> genreCodes,
            List<String> providerIds) {

        int normalizedPreviewSize =
                Math.max(0, previewSize);

        if (normalizedPreviewSize == 0) {

            return new ArrayList<SearchResultVO>();
        }

        SearchResultPageVO firstPage =
                getContentPage(
                        keyword,
                        1,
                        contentPageSize,
                        contentTypes,
                        genreCodes,
                        providerIds
                );

        List<SearchResultVO> resultList =
                firstPage.getResultList();

        if (resultList == null
                || resultList.isEmpty()) {

            return new ArrayList<SearchResultVO>();
        }

        int endIndex =
                Math.min(
                        normalizedPreviewSize,
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
     * 필요한 결과 개수가 모일 때까지
     * 이전 조회 지점 다음 TMDB 페이지부터 이어서 조회한다.
     */
    private void collectUntilRequiredCount(
            SearchCacheEntry cacheEntry,
            int requiredResultCount,
            String keyword,
            List<String> contentTypes,
            List<String> genreCodes,
            List<String> providerIds) {

        while (!cacheEntry.isComplete()
                && cacheEntry.getResultList().size()
                        < requiredResultCount) {

            int tmdbPage =
                    cacheEntry.getNextTmdbPage();

            if (tmdbPage > MAX_TMDB_PAGE) {

                cacheEntry.setComplete(true);
                break;
            }

            SearchResultPageVO partialPage =
                    requestSearchPage(
                            keyword,
                            tmdbPage,
                            contentTypes,
                            genreCodes,
                            providerIds
                    );

            /*
             * 요청한 페이지는 처리한 것으로 보고
             * 다음 TMDB 페이지 번호를 먼저 증가시킨다.
             */
            cacheEntry.setNextTmdbPage(
                    tmdbPage + 1
            );

            if (partialPage == null) {

                cacheEntry.setComplete(true);
                break;
            }

            if (tmdbPage == 1) {

                cacheEntry.setSourceTotalPages(
                        Math.min(
                                partialPage.getTotalPages(),
                                MAX_TMDB_PAGE
                        )
                );

                cacheEntry.setSourceTotalResults(
                        partialPage.getTotalResults()
                );
            }

            addUniqueResults(
                    cacheEntry.getResultList(),
                    partialPage.getResultList()
            );

            int sourceTotalPages =
                    cacheEntry.getSourceTotalPages();

            if (sourceTotalPages <= 0
                    || tmdbPage >= sourceTotalPages
                    || tmdbPage >= MAX_TMDB_PAGE) {

                cacheEntry.setComplete(true);
            }

            cacheEntry.updateAccessTime();
        }
    }

    /**
     * 검색어 유무에 따라 인기 콘텐츠 또는 검색 API를 호출한다.
     */
    private SearchResultPageVO requestSearchPage(
            String keyword,
            int tmdbPage,
            List<String> contentTypes,
            List<String> genreCodes,
            List<String> providerIds) {

        if (keyword == null
                || keyword.isEmpty()) {

            return searchService.getPopularContent(
                    tmdbPage,
                    contentTypes,
                    genreCodes,
                    providerIds
            );
        }

        return searchService.searchByTmdb(
                keyword,
                tmdbPage,
                contentTypes,
                genreCodes,
                providerIds
        );
    }

    /**
     * 누적 캐시에서 현재 화면 페이지에 필요한 범위만 잘라 반환한다.
     */
    private SearchResultPageVO createDisplayPage(
            SearchCacheEntry cacheEntry,
            int displayPage,
            int pageSize) {

        int startIndex =
                (displayPage - 1)
                        * pageSize;

        int endIndex =
                Math.min(
                        startIndex + pageSize,
                        cacheEntry.getResultList().size()
                );

        List<SearchResultVO> displayResultList =
                new ArrayList<SearchResultVO>();

        if (startIndex
                < cacheEntry.getResultList().size()) {

            displayResultList.addAll(
                    cacheEntry.getResultList().subList(
                            startIndex,
                            endIndex
                    )
            );
        }

        SearchResultPageVO pageVO =
                new SearchResultPageVO();

        pageVO.setPage(displayPage);

        pageVO.setResultList(
                displayResultList
        );

        pageVO.setTotalPages(
                calculateDisplayTotalPages(
                        cacheEntry,
                        displayPage,
                        pageSize
                )
        );

        pageVO.setTotalResults(
                cacheEntry.getSourceTotalResults()
        );

        return pageVO;
    }

    /**
     * 화면용 전체 페이지 수를 계산한다.
     *
     * 아직 전체 검색 결과를 모두 수집하지 않은 경우에는
     * 현재 페이지보다 다음 페이지가 존재한다는 범위까지만 노출한다.
     */
    private int calculateDisplayTotalPages(
            SearchCacheEntry cacheEntry,
            int currentPage,
            int pageSize) {

        int collectedCount =
                cacheEntry.getResultList().size();

        int collectedPages =
                (collectedCount + pageSize - 1)
                        / pageSize;

        if (cacheEntry.isComplete()) {

            return collectedPages;
        }

        /*
         * 다음 페이지 확인용 한 건을 수집했으면
         * 현재까지 확인된 페이지 수를 반환한다.
         */
        int currentPageEndIndex =
                currentPage * pageSize;

        if (collectedCount > currentPageEndIndex) {

            return Math.max(
                    collectedPages,
                    currentPage + 1
            );
        }

        /*
         * TMDB의 원본 전체 페이지 수를 표시하면
         * OTT 필터 후 빈 페이지가 생길 수 있으므로
         * 아직 확인된 범위까지만 표시한다.
         */
        return Math.max(
                collectedPages,
                currentPage
        );
    }

    /**
     * TMDB ID와 콘텐츠 타입이 같은 콘텐츠는 중복 추가하지 않는다.
     */
    private void addUniqueResults(
            List<SearchResultVO> targetList,
            List<SearchResultVO> sourceList) {

        if (sourceList == null
                || sourceList.isEmpty()) {

            return;
        }

        for (SearchResultVO sourceVO
                : sourceList) {

            if (sourceVO == null
                    || sourceVO.getTmdbId() == null
                    || sourceVO.getContentType() == null) {

                continue;
            }

            SearchResultVO duplicatedVO =
                    findDuplicatedResult(
                            targetList,
                            sourceVO
                    );

            if (duplicatedVO == null) {

                targetList.add(sourceVO);
                continue;
            }

            /*
             * 제목 검색 결과와 인물 검색 결과가 중복되면
             * 배우 또는 감독 검색 정보를 유지한다.
             */
            if ("PERSON".equals(
                    sourceVO.getMatchType()
            )) {

                duplicatedVO.setMatchType(
                        sourceVO.getMatchType()
                );

                duplicatedVO.setMatchedPersonName(
                        sourceVO.getMatchedPersonName()
                );

                duplicatedVO.setMatchedPersonRole(
                        sourceVO.getMatchedPersonRole()
                );
            }
        }
    }

    private SearchResultVO findDuplicatedResult(
            List<SearchResultVO> targetList,
            SearchResultVO sourceVO) {

        for (SearchResultVO targetVO
                : targetList) {

            if (targetVO == null
                    || targetVO.getTmdbId() == null
                    || targetVO.getContentType() == null) {

                continue;
            }

            boolean sameTmdbId =
                    sourceVO.getTmdbId().equals(
                            targetVO.getTmdbId()
                    );

            boolean sameContentType =
                    sourceVO.getContentType()
                            .equalsIgnoreCase(
                                    targetVO.getContentType()
                            );

            if (sameTmdbId
                    && sameContentType) {

                return targetVO;
            }
        }

        return null;
    }

    /**
     * 검색 조건에 해당하는 캐시 엔트리를 반환한다.
     */
    private SearchCacheEntry getOrCreateCacheEntry(
            String cacheKey) {

        removeExpiredEntries();

        if (searchCache.size() >= MAX_CACHE_SIZE) {

            removeOldestEntry();
        }

        return searchCache.compute(
                cacheKey,
                (key, oldEntry) -> {

                    if (oldEntry == null
                            || oldEntry.isExpired()) {

                        return new SearchCacheEntry();
                    }

                    oldEntry.updateAccessTime();

                    return oldEntry;
                }
        );
    }

    /**
     * 만료된 검색 결과 캐시를 제거한다.
     */
    private void removeExpiredEntries() {

        searchCache.entrySet().removeIf(
                entry -> entry.getValue() == null
                        || entry.getValue().isExpired()
        );
    }

    /**
     * 캐시 최대 개수를 초과하면
     * 가장 오래 사용하지 않은 검색 조건 하나를 제거한다.
     */
    private void removeOldestEntry() {

        String oldestKey =
                searchCache.entrySet()
                        .stream()
                        .filter(entry ->
                                entry.getValue() != null
                        )
                        .min(
                                Comparator.comparingLong(
                                        entry ->
                                                entry.getValue()
                                                        .getLastAccessTime()
                                )
                        )
                        .map(Map.Entry::getKey)
                        .orElse(null);

        if (oldestKey != null) {

            searchCache.remove(oldestKey);
        }
    }

    private String createCacheKey(
            String keyword,
            List<String> contentTypes,
            List<String> genreCodes,
            List<String> providerIds) {

        return keyword.toLowerCase()
                + "|TYPE="
                + String.join(",", contentTypes)
                + "|GENRE="
                + String.join(",", genreCodes)
                + "|PROVIDER="
                + String.join(",", providerIds);
    }

    private List<String> normalizeAndSortList(
            List<String> sourceList) {

        List<String> normalizedList =
                new ArrayList<String>();

        if (sourceList == null) {

            return normalizedList;
        }

        for (String value : sourceList) {

            if (value == null) {
                continue;
            }

            String normalizedValue =
                    value.trim();

            if (normalizedValue.isEmpty()) {
                continue;
            }

            if (!normalizedList.contains(
                    normalizedValue
            )) {

                normalizedList.add(
                        normalizedValue
                );
            }
        }

        Collections.sort(
                normalizedList,
                String.CASE_INSENSITIVE_ORDER
        );

        return normalizedList;
    }

    private String normalizeKeyword(
            String keyword) {

        if (keyword == null) {

            return "";
        }

        return keyword.trim();
    }

    private int normalizePage(
            int page) {

        return page <= 0
                ? 1
                : page;
    }

    private int normalizePageSize(
            int pageSize) {

        if (pageSize <= 0) {

            return 10;
        }

        return Math.min(
                pageSize,
                100
        );
    }

    /**
     * 검색 조건별 누적 결과 캐시 객체
     */
    private static class SearchCacheEntry {

        private final List<SearchResultVO> resultList;

        /*
         * 다음에 호출할 TMDB 페이지 번호
         */
        private int nextTmdbPage;

        private int sourceTotalPages;
        private int sourceTotalResults;

        private boolean complete;

        private long createdTime;
        private long lastAccessTime;

        private SearchCacheEntry() {

            this.resultList =
                    new ArrayList<SearchResultVO>();

            reset();
        }

        private void reset() {

            resultList.clear();

            nextTmdbPage = 1;
            sourceTotalPages = 0;
            sourceTotalResults = 0;

            complete = false;

            createdTime =
                    System.currentTimeMillis();

            lastAccessTime =
                    createdTime;
        }

        private boolean isExpired() {

            return System.currentTimeMillis()
                    - createdTime
                    >= CACHE_TTL_MILLIS;
        }

        private List<SearchResultVO> getResultList() {
            return resultList;
        }

        private int getNextTmdbPage() {
            return nextTmdbPage;
        }

        private void setNextTmdbPage(
                int nextTmdbPage) {

            this.nextTmdbPage =
                    nextTmdbPage;
        }

        private int getSourceTotalPages() {
            return sourceTotalPages;
        }

        private void setSourceTotalPages(
                int sourceTotalPages) {

            this.sourceTotalPages =
                    sourceTotalPages;
        }

        private int getSourceTotalResults() {
            return sourceTotalResults;
        }

        private void setSourceTotalResults(
                int sourceTotalResults) {

            this.sourceTotalResults =
                    sourceTotalResults;
        }

        private boolean isComplete() {
            return complete;
        }

        private void setComplete(
                boolean complete) {

            this.complete =
                    complete;
        }

        private long getLastAccessTime() {
            return lastAccessTime;
        }

        private void updateAccessTime() {

            this.lastAccessTime =
                    System.currentTimeMillis();
        }
    }
}