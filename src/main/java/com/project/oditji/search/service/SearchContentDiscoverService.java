package com.project.oditji.search.service;

import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.project.oditji.common.util.DateTimeUtil;
import com.project.oditji.common.util.TmdbGenreUtil;
import com.project.oditji.search.vo.CachedContentVO;

/**
 * TMDB Discover API를 이용해 영화와 TV 수집 후보를 생성합니다.
 *
 * provider 기반 기본 수집과 최근 콘텐츠 보완 수집을 담당하며,
 * 상세정보와 실제 한국 OTT 검증은 SearchContentEnrichmentService에 위임합니다.
 */
@Service
public class SearchContentDiscoverService {

    private static final String MOVIE = "MOVIE";
    private static final String TV = "TV";
    private static final int TMDB_MAX_PAGE = 500;

    private static final String API_TYPE_MOVIE = "movie";
    private static final String JSON_RESULTS = "results";
    private static final String JSON_ORIGINAL_TITLE = "original_title";
    private static final String JSON_ORIGINAL_NAME = "original_name";

    private static final Map<Integer, String> MOVIE_GENRES = TmdbGenreUtil.movieGenres();
    private static final Map<Integer, String> TV_GENRES = TmdbGenreUtil.tvGenres();

    @Value("${search.content-cache.start-year:1950}")
    private int startYear;

    @Value("${search.content-cache.supplement-enabled:true}")
    private boolean supplementEnabled;

    @Value("${search.content-cache.supplement-years:5}")
    private int supplementYears;

    @Value("${search.content-cache.supplement-pages-per-year:10}")
    private int supplementPagesPerYear;

    @Value("${search.content-cache.supplement-max-candidates-per-type:1000}")
    private int supplementMaxCandidatesPerType;

    private final TmdbApiClient apiClient;
    private final SearchContentPolicyService contentPolicyService;

    public SearchContentDiscoverService(
            TmdbApiClient apiClient,
            SearchContentPolicyService contentPolicyService) {

        this.apiClient = apiClient;
        this.contentPolicyService = contentPolicyService;
    }

    public List<CachedContentVO> collectMovieCandidates(
            int targetCount,
            Set<Integer> providerIds) {
        return collectDiscoverCandidates(API_TYPE_MOVIE, MOVIE, targetCount, providerIds);
    }

    public List<CachedContentVO> collectTvCandidates(
            int targetCount,
            Set<Integer> providerIds) {
        return collectDiscoverCandidates("tv", TV, targetCount, providerIds);
    }

    public List<CachedContentVO> collectSupplementCandidates() {
        if (!supplementEnabled) {
            return new ArrayList<CachedContentVO>();
        }

        int normalizedYears = Math.clamp(supplementYears, 1, 10);
        int normalizedPages = Math.clamp(supplementPagesPerYear, 1, 30);
        int normalizedMax = Math.clamp(
                supplementMaxCandidatesPerType,
                0,
                5000
        );

        if (normalizedMax == 0) {
            return new ArrayList<CachedContentVO>();
        }

        List<CachedContentVO> result = new ArrayList<CachedContentVO>();
        result.addAll(collectSupplementByType(
                API_TYPE_MOVIE,
                MOVIE,
                normalizedYears,
                normalizedPages,
                normalizedMax
        ));
        result.addAll(collectSupplementByType(
                "tv",
                TV,
                normalizedYears,
                normalizedPages,
                normalizedMax
        ));

        return removeDuplicate(result);
    }

    private List<CachedContentVO> collectDiscoverCandidates(
            String apiType,
            String contentType,
            int targetCount,
            Set<Integer> providerIds) {

        List<CachedContentVO> result =
                new ArrayList<CachedContentVO>();

        if (targetCount <= 0
                || providerIds == null
                || providerIds.isEmpty()) {
            return result;
        }

        String providerFilter =
                joinProviderIds(providerIds);

        int currentYear =
                Year.now(DateTimeUtil.KOREA_ZONE).getValue() + 1;

        for (int year = currentYear;
             year >= startYear && result.size() < targetCount;
             year--) {

            collectDiscoverYear(
                    result,
                    apiType,
                    contentType,
                    targetCount,
                    providerFilter,
                    year
            );
        }

        return result;
    }

    private void collectDiscoverYear(
            List<CachedContentVO> result,
            String apiType,
            String contentType,
            int targetCount,
            String providerFilter,
            int year) {

        int page = 1;
        int totalPages = 1;

        while (page <= totalPages
                && page <= TMDB_MAX_PAGE
                && result.size() < targetCount) {

            JSONObject root =
                    apiClient.get(
                            buildDiscoverUrl(
                                    apiType,
                                    year,
                                    page,
                                    providerFilter
                            )
                    );

            totalPages = Math.min(
                    root.optInt("total_pages", 0),
                    TMDB_MAX_PAGE
            );

            if (!appendDiscoverItems(
                    result,
                    root.optJSONArray(JSON_RESULTS),
                    contentType,
                    targetCount
            )) {
                break;
            }

            page++;
        }
    }

    private List<CachedContentVO> collectSupplementByType(
            String apiType,
            String contentType,
            int yearCount,
            int pagesPerYear,
            int maxCandidates) {

        List<CachedContentVO> result =
                new ArrayList<CachedContentVO>();

        int currentYear =
                Year.now(DateTimeUtil.KOREA_ZONE).getValue() + 1;

        int lastYear =
                Math.max(
                        startYear,
                        currentYear - yearCount + 1
                );

        for (int year = currentYear;
             year >= lastYear && result.size() < maxCandidates;
             year--) {

            collectSupplementYear(
                    result,
                    apiType,
                    contentType,
                    pagesPerYear,
                    maxCandidates,
                    year
            );
        }

        return removeDuplicate(result);
    }

    private void collectSupplementYear(
            List<CachedContentVO> result,
            String apiType,
            String contentType,
            int pagesPerYear,
            int maxCandidates,
            int year) {

        for (int page = 1;
             page <= pagesPerYear && result.size() < maxCandidates;
             page++) {

            JSONObject root =
                    apiClient.get(
                            buildSupplementDiscoverUrl(
                                    apiType,
                                    year,
                                    page
                            )
                    );

            int totalPages =
                    Math.min(
                            root.optInt("total_pages", 0),
                            pagesPerYear
                    );

            if (totalPages <= 0
                    || page > totalPages
                    || !appendDiscoverItems(
                            result,
                            root.optJSONArray(JSON_RESULTS),
                            contentType,
                            maxCandidates
                    )) {
                break;
            }
        }
    }

    private boolean appendDiscoverItems(
            List<CachedContentVO> result,
            JSONArray items,
            String contentType,
            int maxCandidates) {

        if (items == null
                || items.length() == 0) {
            return false;
        }

        for (int index = 0;
             index < items.length() && result.size() < maxCandidates;
             index++) {

            appendDiscoverItem(
                    result,
                    items.optJSONObject(index),
                    contentType
            );
        }

        return true;
    }

    private void appendDiscoverItem(
            List<CachedContentVO> result,
            JSONObject item,
            String contentType) {

        if (item == null
                || shouldExcludeContent(item)) {
            return;
        }

        CachedContentVO content =
                convertDiscoverItem(
                        item,
                        contentType
                );

        if (content.getTmdbId() != null) {
            result.add(content);
        }
    }

    private String buildDiscoverUrl(
            String apiType,
            int year,
            int page,
            String providerFilter) {

        StringBuilder url = new StringBuilder(apiClient.getBaseUrl())
                .append("/discover/")
                .append(apiType)
                .append("?language=")
                .append(apiClient.encode(apiClient.getLanguage()))
                .append("&watch_region=")
                .append(apiClient.encode(apiClient.getRegion()))
                .append("&with_watch_monetization_types=flatrate")
                .append("&with_watch_providers=")
                .append(apiClient.encode(providerFilter))
                .append("&include_adult=false")
                .append("&sort_by=popularity.desc")
                .append("&page=")
                .append(page);

        appendYearFilter(url, apiType, year);
        return url.toString();
    }

    private String buildSupplementDiscoverUrl(
            String apiType,
            int year,
            int page) {

        StringBuilder url = new StringBuilder(apiClient.getBaseUrl())
                .append("/discover/")
                .append(apiType)
                .append("?language=")
                .append(apiClient.encode(apiClient.getLanguage()))
                .append("&include_adult=false")
                .append("&sort_by=popularity.desc")
                .append("&page=")
                .append(page);

        appendYearFilter(url, apiType, year);
        return url.toString();
    }

    private void appendYearFilter(
            StringBuilder url,
            String apiType,
            int year) {

        if (API_TYPE_MOVIE.equals(apiType)) {
            url.append("&region=")
                    .append(apiClient.encode(apiClient.getRegion()))
                    .append("&primary_release_year=")
                    .append(year)
                    .append("&include_video=false");
        } else {
            url.append("&first_air_date_year=").append(year);
        }
    }

    private String joinProviderIds(Set<Integer> providerIds) {
        StringJoiner joiner = new StringJoiner("|");
        for (Integer providerId : providerIds) {
            if (providerId != null && providerId > 0) {
                joiner.add(String.valueOf(providerId));
            }
        }
        return joiner.toString();
    }

    private CachedContentVO convertDiscoverItem(
            JSONObject item,
            String contentType) {

        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(nullableLong(item, "id"));
        content.setContentType(contentType);

        if (MOVIE.equals(contentType)) {
            content.setTitle(firstNonBlank(
                    nullableString(item, "title"),
                    nullableString(item, JSON_ORIGINAL_TITLE)
            ));
            content.setOriginalTitle(nullableString(item, JSON_ORIGINAL_TITLE));
            content.setReleaseDate(nullableString(item, "release_date"));
        } else {
            content.setTitle(firstNonBlank(
                    nullableString(item, "name"),
                    nullableString(item, JSON_ORIGINAL_NAME)
            ));
            content.setOriginalTitle(nullableString(item, JSON_ORIGINAL_NAME));
            content.setReleaseDate(nullableString(item, "first_air_date"));
        }

        content.setPosterPath(nullableString(item, "poster_path"));
        content.setGenreText(convertGenreIdsToText(
                item.optJSONArray("genre_ids"),
                contentType
        ));
        content.setTmdbScore(nullableDouble(item, "vote_average"));
        content.setPopularity(nullableDouble(item, "popularity"));
        return content;
    }

    private String convertGenreIdsToText(
            JSONArray genreIds,
            String contentType) {

        if (genreIds == null) {
            return null;
        }

        Map<Integer, String> genreMap = MOVIE.equals(contentType)
                ? MOVIE_GENRES
                : TV_GENRES;

        List<String> names = new ArrayList<String>();
        for (int index = 0; index < genreIds.length(); index++) {
            String name = genreMap.get(genreIds.optInt(index, -1));
            if (name != null && !names.contains(name)) {
                names.add(name);
            }
        }

        return names.isEmpty() ? null : String.join(", ", names);
    }

    /**
     * TMDB의 adult 플래그와 ODITJI 제목 정책을 함께 적용합니다.
     *
     * adult=false로 내려오는 데이터 중에서도 명백한 포르노성 제목은
     * 공용 정책 서비스에서 추가로 제외합니다.
     */
    private boolean shouldExcludeContent(JSONObject item) {
        if (item.optBoolean("adult", false)) {
            return true;
        }

        String title = firstNonBlank(
                item.optString("title", null),
                item.optString("name", null)
        );
        String originalTitle = firstNonBlank(
                item.optString(JSON_ORIGINAL_TITLE, null),
                item.optString(JSON_ORIGINAL_NAME, null)
        );

        return contentPolicyService.shouldExcludeContent(
                null,
                null,
                title,
                originalTitle
        );
    }

    private List<CachedContentVO> removeDuplicate(List<CachedContentVO> source) {
        Map<String, CachedContentVO> map = new LinkedHashMap<String, CachedContentVO>();
        for (CachedContentVO content : source) {
            if (content != null
                    && content.getTmdbId() != null
                    && content.getContentType() != null) {
                map.put(content.createContentKey(), content);
            }
        }
        return new ArrayList<CachedContentVO>(map.values());
    }

    private String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }

    private String nullableString(JSONObject json, String key) {
        if (!json.has(key) || json.isNull(key)) {
            return null;
        }
        String value = json.optString(key, null);
        return value == null || value.isBlank() ? null : value;
    }

    private Long nullableLong(JSONObject json, String key) {
        if (!json.has(key) || json.isNull(key)) {
            return null;
        }
        long value = json.optLong(key, 0L);
        return value <= 0 ? null : value;
    }

    private Double nullableDouble(JSONObject json, String key) {
        if (!json.has(key) || json.isNull(key)) {
            return null;
        }
        return json.optDouble(key);
    }


}
