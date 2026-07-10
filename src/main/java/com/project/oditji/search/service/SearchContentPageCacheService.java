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

    private static final long CACHE_TTL_MILLIS =
            Duration.ofMinutes(10).toMillis();

    private static final int MAX_CACHE_SIZE = 200;
    private static final int MAX_TMDB_PAGE = 500;

    private final SearchService searchService;

    private final Map<String, SearchCacheEntry> searchCache =
            new ConcurrentHashMap<String, SearchCacheEntry>();

    public SearchContentPageCacheService(SearchService searchService) {
        this.searchService = searchService;
    }

    public SearchResultPageVO getContentPage(
            String keyword,
            int displayPage,
            int pageSize,
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds) {

        int normalizedPage = displayPage <= 0 ? 1 : displayPage;
        int normalizedPageSize = pageSize <= 0 ? 10 : Math.min(pageSize, 100);
        String normalizedKeyword = keyword == null ? "" : keyword.trim();

        List<String> normalizedCategories = normalizeAndSortList(contentCategories);
        List<String> normalizedGenres = normalizeAndSortList(genreCodes);
        List<String> normalizedProviders = normalizeAndSortList(providerIds);

        String cacheKey = createCacheKey(
                normalizedKeyword,
                normalizedCategories,
                normalizedGenres,
                normalizedProviders
        );

        SearchCacheEntry cacheEntry = getOrCreateCacheEntry(cacheKey);

        int startIndex = (normalizedPage - 1) * normalizedPageSize;
        int requiredResultCount = startIndex + normalizedPageSize + 1;

        synchronized (cacheEntry) {
            if (cacheEntry.isExpired()) {
                cacheEntry.reset();
            }

            collectUntilRequiredCount(
                    cacheEntry,
                    requiredResultCount,
                    normalizedKeyword,
                    normalizedCategories,
                    normalizedGenres,
                    normalizedProviders
            );

            return createDisplayPage(
                    cacheEntry,
                    normalizedPage,
                    normalizedPageSize
            );
        }
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

        SearchResultPageVO firstPage = getContentPage(
                keyword,
                1,
                contentPageSize,
                contentCategories,
                genreCodes,
                providerIds
        );

        List<SearchResultVO> resultList = firstPage.getResultList();

        if (resultList == null || resultList.isEmpty()) {
            return new ArrayList<SearchResultVO>();
        }

        int endIndex = Math.min(previewSize, resultList.size());

        return new ArrayList<SearchResultVO>(
                resultList.subList(0, endIndex)
        );
    }

    private void collectUntilRequiredCount(
            SearchCacheEntry cacheEntry,
            int requiredResultCount,
            String keyword,
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds) {

        while (!cacheEntry.isComplete()
                && cacheEntry.getResultList().size() < requiredResultCount) {

            int tmdbPage = cacheEntry.getNextTmdbPage();

            if (tmdbPage > MAX_TMDB_PAGE) {
                cacheEntry.setComplete(true);
                break;
            }

            SearchResultPageVO partialPage = requestSearchPage(
                    keyword,
                    tmdbPage,
                    contentCategories,
                    genreCodes,
                    providerIds
            );

            cacheEntry.setNextTmdbPage(tmdbPage + 1);

            if (partialPage == null) {
                cacheEntry.setComplete(true);
                break;
            }

            if (tmdbPage == 1) {
                cacheEntry.setSourceTotalPages(
                        Math.min(partialPage.getTotalPages(), MAX_TMDB_PAGE)
                );
                cacheEntry.setSourceTotalResults(partialPage.getTotalResults());
            }

            addUniqueResults(
                    cacheEntry.getResultList(),
                    partialPage.getResultList()
            );

            int sourceTotalPages = cacheEntry.getSourceTotalPages();

            if (sourceTotalPages <= 0
                    || tmdbPage >= sourceTotalPages
                    || tmdbPage >= MAX_TMDB_PAGE) {
                cacheEntry.setComplete(true);
            }

            cacheEntry.updateAccessTime();
        }
    }

    private SearchResultPageVO requestSearchPage(
            String keyword,
            int tmdbPage,
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds) {

        if (keyword == null || keyword.isEmpty()) {
            return searchService.getPopularContent(
                    tmdbPage,
                    contentCategories,
                    genreCodes,
                    providerIds
            );
        }

        return searchService.searchByTmdb(
                keyword,
                tmdbPage,
                contentCategories,
                genreCodes,
                providerIds
        );
    }

    private SearchResultPageVO createDisplayPage(
            SearchCacheEntry cacheEntry,
            int displayPage,
            int pageSize) {

        int startIndex = (displayPage - 1) * pageSize;
        int endIndex = Math.min(
                startIndex + pageSize,
                cacheEntry.getResultList().size()
        );

        List<SearchResultVO> displayResultList =
                new ArrayList<SearchResultVO>();

        if (startIndex < cacheEntry.getResultList().size()) {
            displayResultList.addAll(
                    cacheEntry.getResultList().subList(startIndex, endIndex)
            );
        }

        SearchResultPageVO pageVO = new SearchResultPageVO();
        pageVO.setPage(displayPage);
        pageVO.setResultList(displayResultList);
        pageVO.setTotalPages(
                calculateDisplayTotalPages(cacheEntry, displayPage, pageSize)
        );
        pageVO.setTotalResults(cacheEntry.getSourceTotalResults());

        return pageVO;
    }

    private int calculateDisplayTotalPages(
            SearchCacheEntry cacheEntry,
            int currentPage,
            int pageSize) {

        int collectedCount = cacheEntry.getResultList().size();
        int collectedPages = (collectedCount + pageSize - 1) / pageSize;

        if (cacheEntry.isComplete()) {
            return collectedPages;
        }

        if (collectedCount > currentPage * pageSize) {
            return Math.max(collectedPages, currentPage + 1);
        }

        return Math.max(collectedPages, currentPage);
    }

    private void addUniqueResults(
            List<SearchResultVO> targetList,
            List<SearchResultVO> sourceList) {

        if (sourceList == null) {
            return;
        }

        for (SearchResultVO sourceVO : sourceList) {
            if (sourceVO == null
                    || sourceVO.getTmdbId() == null
                    || sourceVO.getContentType() == null) {
                continue;
            }

            SearchResultVO duplicatedVO = findDuplicatedResult(
                    targetList,
                    sourceVO
            );

            if (duplicatedVO == null) {
                targetList.add(sourceVO);
                continue;
            }

            if ("PERSON".equals(sourceVO.getMatchType())) {
                duplicatedVO.setMatchType(sourceVO.getMatchType());
                duplicatedVO.setMatchedPersonName(sourceVO.getMatchedPersonName());
                duplicatedVO.setMatchedPersonRole(sourceVO.getMatchedPersonRole());
            }
        }
    }

    private SearchResultVO findDuplicatedResult(
            List<SearchResultVO> targetList,
            SearchResultVO sourceVO) {

        for (SearchResultVO targetVO : targetList) {
            if (targetVO == null
                    || targetVO.getTmdbId() == null
                    || targetVO.getContentType() == null) {
                continue;
            }

            if (sourceVO.getTmdbId().equals(targetVO.getTmdbId())
                    && sourceVO.getContentType().equalsIgnoreCase(
                            targetVO.getContentType()
                    )) {
                return targetVO;
            }
        }

        return null;
    }

    private SearchCacheEntry getOrCreateCacheEntry(String cacheKey) {
        removeExpiredEntries();

        if (searchCache.size() >= MAX_CACHE_SIZE) {
            removeOldestEntry();
        }

        return searchCache.compute(
                cacheKey,
                (key, oldEntry) -> {
                    if (oldEntry == null || oldEntry.isExpired()) {
                        return new SearchCacheEntry();
                    }
                    oldEntry.updateAccessTime();
                    return oldEntry;
                }
        );
    }

    private void removeExpiredEntries() {
        searchCache.entrySet().removeIf(
                entry -> entry.getValue() == null
                        || entry.getValue().isExpired()
        );
    }

    private void removeOldestEntry() {
        String oldestKey = searchCache.entrySet()
                .stream()
                .filter(entry -> entry.getValue() != null)
                .min(
                        Comparator.comparingLong(
                                entry -> entry.getValue().getLastAccessTime()
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
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds) {

        return keyword.toLowerCase()
                + "|CATEGORY=" + String.join(",", contentCategories)
                + "|GENRE=" + String.join(",", genreCodes)
                + "|PROVIDER=" + String.join(",", providerIds);
    }

    private List<String> normalizeAndSortList(List<String> sourceList) {
        List<String> normalizedList = new ArrayList<String>();

        if (sourceList == null) {
            return normalizedList;
        }

        for (String value : sourceList) {
            if (value == null) {
                continue;
            }

            String normalizedValue = value.trim().toUpperCase();

            if (!normalizedValue.isEmpty()
                    && !normalizedList.contains(normalizedValue)) {
                normalizedList.add(normalizedValue);
            }
        }

        Collections.sort(normalizedList);
        return normalizedList;
    }

    private static class SearchCacheEntry {

        private final List<SearchResultVO> resultList;
        private int nextTmdbPage;
        private int sourceTotalPages;
        private int sourceTotalResults;
        private boolean complete;
        private long createdTime;
        private long lastAccessTime;

        private SearchCacheEntry() {
            this.resultList = new ArrayList<SearchResultVO>();
            reset();
        }

        private void reset() {
            resultList.clear();
            nextTmdbPage = 1;
            sourceTotalPages = 0;
            sourceTotalResults = 0;
            complete = false;
            createdTime = System.currentTimeMillis();
            lastAccessTime = createdTime;
        }

        private boolean isExpired() {
            return System.currentTimeMillis() - createdTime >= CACHE_TTL_MILLIS;
        }

        private List<SearchResultVO> getResultList() {
            return resultList;
        }

        private int getNextTmdbPage() {
            return nextTmdbPage;
        }

        private void setNextTmdbPage(int nextTmdbPage) {
            this.nextTmdbPage = nextTmdbPage;
        }

        private int getSourceTotalPages() {
            return sourceTotalPages;
        }

        private void setSourceTotalPages(int sourceTotalPages) {
            this.sourceTotalPages = sourceTotalPages;
        }

        private int getSourceTotalResults() {
            return sourceTotalResults;
        }

        private void setSourceTotalResults(int sourceTotalResults) {
            this.sourceTotalResults = sourceTotalResults;
        }

        private boolean isComplete() {
            return complete;
        }

        private void setComplete(boolean complete) {
            this.complete = complete;
        }

        private long getLastAccessTime() {
            return lastAccessTime;
        }

        private void updateAccessTime() {
            this.lastAccessTime = System.currentTimeMillis();
        }
    }
}
