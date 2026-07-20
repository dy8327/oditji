package com.project.oditji.search.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

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

        if (CATEGORY_DOCUMENTARY.equals(category)) {
            return documentary;
        }

        return false;
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

    private Set<String> providerIdsToKeys(
            List<String> providerIds) {

        Set<String> result =
                new HashSet<String>();

        if (providerIds == null) {
            return result;
        }

        for (String providerId
                : providerIds) {

            if ("8".equals(providerId)) {
                result.add("netflix");

            } else if ("1883".equals(providerId)) {
                result.add("tving");

            } else if ("356".equals(providerId)) {
                result.add("wavve");

            } else if ("337".equals(providerId)) {
                result.add("disney");

            } else if ("97".equals(providerId)) {
                result.add("watcha");

            } else if ("283".equals(providerId)) {
                result.add("coupang");
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