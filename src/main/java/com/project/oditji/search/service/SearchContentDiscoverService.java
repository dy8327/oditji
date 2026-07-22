package com.project.oditji.search.service;

import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    private static final Map<Integer, String> MOVIE_GENRES = createMovieGenreMap();
    private static final Map<Integer, String> TV_GENRES = createTvGenreMap();

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

    public SearchContentDiscoverService(TmdbApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public List<CachedContentVO> collectMovieCandidates(
            int targetCount,
            Set<Integer> providerIds) {
        return collectDiscoverCandidates("movie", MOVIE, targetCount, providerIds);
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

        int normalizedYears = Math.max(1, Math.min(supplementYears, 10));
        int normalizedPages = Math.max(1, Math.min(supplementPagesPerYear, 30));
        int normalizedMax = Math.max(
                0,
                Math.min(supplementMaxCandidatesPerType, 5000)
        );

        if (normalizedMax == 0) {
            return new ArrayList<CachedContentVO>();
        }

        List<CachedContentVO> result = new ArrayList<CachedContentVO>();
        result.addAll(collectSupplementByType(
                "movie",
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

        List<CachedContentVO> result = new ArrayList<CachedContentVO>();

        if (targetCount <= 0 || providerIds == null || providerIds.isEmpty()) {
            return result;
        }

        String providerFilter = joinProviderIds(providerIds);
        int currentYear = Year.now().getValue() + 1;

        for (int year = currentYear;
             year >= startYear && result.size() < targetCount;
             year--) {

            int page = 1;
            int totalPages = 1;

            while (page <= totalPages
                    && page <= TMDB_MAX_PAGE
                    && result.size() < targetCount) {

                JSONObject root = apiClient.get(
                        buildDiscoverUrl(apiType, year, page, providerFilter)
                );

                totalPages = Math.min(
                        root.optInt("total_pages", 0),
                        TMDB_MAX_PAGE
                );

                JSONArray items = root.optJSONArray("results");
                if (items == null || items.isEmpty()) {
                    break;
                }

                for (int index = 0;
                     index < items.length() && result.size() < targetCount;
                     index++) {

                    JSONObject item = items.optJSONObject(index);
                    if (item == null || shouldExcludeContent(item)) {
                        continue;
                    }

                    CachedContentVO content = convertDiscoverItem(item, contentType);
                    if (content.getTmdbId() != null) {
                        result.add(content);
                    }
                }

                page++;
            }
        }

        return result;
    }

    private List<CachedContentVO> collectSupplementByType(
            String apiType,
            String contentType,
            int yearCount,
            int pagesPerYear,
            int maxCandidates) {

        List<CachedContentVO> result = new ArrayList<CachedContentVO>();
        int currentYear = Year.now().getValue() + 1;
        int lastYear = Math.max(startYear, currentYear - yearCount + 1);

        for (int year = currentYear;
             year >= lastYear && result.size() < maxCandidates;
             year--) {

            for (int page = 1;
                 page <= pagesPerYear && result.size() < maxCandidates;
                 page++) {

                JSONObject root = apiClient.get(
                        buildSupplementDiscoverUrl(apiType, year, page)
                );

                int totalPages = Math.min(
                        root.optInt("total_pages", 0),
                        pagesPerYear
                );

                if (totalPages <= 0 || page > totalPages) {
                    break;
                }

                JSONArray items = root.optJSONArray("results");
                if (items == null || items.isEmpty()) {
                    break;
                }

                for (int index = 0;
                     index < items.length() && result.size() < maxCandidates;
                     index++) {

                    JSONObject item = items.optJSONObject(index);
                    if (item == null || shouldExcludeContent(item)) {
                        continue;
                    }

                    CachedContentVO content = convertDiscoverItem(item, contentType);
                    if (content.getTmdbId() != null) {
                        result.add(content);
                    }
                }
            }
        }

        return removeDuplicate(result);
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

        if ("movie".equals(apiType)) {
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
                    nullableString(item, "original_title")
            ));
            content.setOriginalTitle(nullableString(item, "original_title"));
            content.setReleaseDate(nullableString(item, "release_date"));
        } else {
            content.setTitle(firstNonBlank(
                    nullableString(item, "name"),
                    nullableString(item, "original_name")
            ));
            content.setOriginalTitle(nullableString(item, "original_name"));
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

    private boolean shouldExcludeContent(JSONObject item) {
        if (item.optBoolean("adult", false)) {
            return true;
        }

        String title = firstNonBlank(
                item.optString("title", null),
                item.optString("name", null)
        );
        String originalTitle = firstNonBlank(
                item.optString("original_title", null),
                item.optString("original_name", null)
        );

        String checkText = normalizeSearchText(
                safeText(title) + " " + safeText(originalTitle)
        );

        String[] blockedKeywords = {
                "성인영화", "에로영화", "에로틱", "포르노", "porn",
                "porno", "adultmovie", "섹스무비", "무삭제판",
                "19금에로", "바람난형수님", "형수님참교육"
        };

        for (String keyword : blockedKeywords) {
            if (checkText.contains(normalizeSearchText(keyword))) {
                return true;
            }
        }

        return false;
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

    private String normalizeSearchText(String value) {
        if (value == null) {
            return "";
        }

        return java.text.Normalizer.normalize(
                        value,
                        java.text.Normalizer.Form.NFKC
                )
                .toLowerCase(java.util.Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]", "");
    }

    private String safeText(String value) {
        return value == null ? "" : value;
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

    private static Map<Integer, String> createMovieGenreMap() {
        Map<Integer, String> map = new LinkedHashMap<Integer, String>();
        map.put(28, "액션");
        map.put(12, "모험");
        map.put(16, "애니메이션");
        map.put(35, "코미디");
        map.put(80, "범죄");
        map.put(99, "다큐멘터리");
        map.put(18, "드라마");
        map.put(10751, "가족");
        map.put(14, "판타지");
        map.put(36, "역사");
        map.put(27, "공포");
        map.put(10402, "음악");
        map.put(9648, "미스터리");
        map.put(10749, "로맨스");
        map.put(878, "SF");
        map.put(10770, "TV 영화");
        map.put(53, "스릴러");
        map.put(10752, "전쟁");
        map.put(37, "서부");
        return Collections.unmodifiableMap(map);
    }

    private static Map<Integer, String> createTvGenreMap() {
        Map<Integer, String> map = new LinkedHashMap<Integer, String>();
        map.put(10759, "액션·모험");
        map.put(16, "애니메이션");
        map.put(35, "코미디");
        map.put(80, "범죄");
        map.put(99, "다큐멘터리");
        map.put(18, "드라마");
        map.put(10751, "가족");
        map.put(10762, "키즈");
        map.put(9648, "미스터리");
        map.put(10763, "뉴스");
        map.put(10764, "리얼리티");
        map.put(10765, "SF·판타지");
        map.put(10766, "연속극");
        map.put(10767, "토크");
        map.put(10768, "전쟁·정치");
        map.put(37, "서부");
        return Collections.unmodifiableMap(map);
    }
}
