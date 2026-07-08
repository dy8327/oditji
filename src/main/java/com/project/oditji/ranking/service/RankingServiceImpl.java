package com.project.oditji.ranking.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.oditji.search.vo.SearchResultVO;

@Service
public class RankingServiceImpl implements RankingService {

    private static final String CONTENT_TYPE_MOVIE = "MOVIE";
    private static final String CONTENT_TYPE_TV = "TV";

    private static final String API_TYPE_MOVIE = "movie";
    private static final String API_TYPE_TV = "tv";

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 100;

    /*
     * 랭킹 정확도를 높이기 위해 영화와 TV를 각각 여러 페이지 조회합니다.
     * 페이지당 약 20건이므로 3페이지씩 조회하면
     * 영화 약 60건, TV 약 60건을 합쳐 정렬할 수 있습니다.
     */
    private static final int RANKING_API_PAGE_COUNT = 3;

    private static final List<String> SUPPORTED_PLATFORM_LIST =
            List.of(
                    "Netflix",
                    "TVING",
                    "wavve",
                    "Disney Plus",
                    "Watcha"
            );

    @Value("${tmdb.api.token}")
    private String token;

    @Value("${tmdb.api.base-url:https://api.themoviedb.org/3}")
    private String tmdbApiBaseUrl;

    @Value("${tmdb.api.language:ko-KR}")
    private String tmdbApiLanguage;

    @Override
    public List<SearchResultVO> getOverallPopularRanking(int limit) {

        int normalizedLimit = normalizeLimit(limit);

        /*
         * 전체 랭킹에서는 ODITJI가 지원하는 5개 OTT의
         * Provider ID를 모두 사용합니다.
         */
        List<SearchResultVO> rankingList =
                getCombinedRanking(
                        SUPPORTED_PLATFORM_LIST,
                        normalizedLimit);

        return rankingList;
    }

    @Override
    public List<SearchResultVO> getPlatformPopularRanking(
            String platformName,
            int limit) {

        int normalizedLimit = normalizeLimit(limit);

        String normalizedPlatformName =
                normalizePlatformName(platformName);

        if (normalizedPlatformName == null) {
            return new ArrayList<SearchResultVO>();
        }

        List<String> platformList = new ArrayList<String>();
        platformList.add(normalizedPlatformName);

        return getCombinedRanking(
                platformList,
                normalizedLimit);
    }

    @Override
    public Map<String, List<SearchResultVO>>
            getAllPlatformPopularRankings(int limit) {

        int normalizedLimit = normalizeLimit(limit);

        Map<String, List<SearchResultVO>> rankingMap =
                new LinkedHashMap<String, List<SearchResultVO>>();

        for (String platformName : SUPPORTED_PLATFORM_LIST) {

            List<SearchResultVO> rankingList =
                    getPlatformPopularRanking(
                            platformName,
                            normalizedLimit);

            rankingMap.put(
                    platformName,
                    rankingList);
        }

        return rankingMap;
    }

    private List<SearchResultVO> getCombinedRanking(
            List<String> platformList,
            int limit) {

        List<SearchResultVO> combinedList =
                new ArrayList<SearchResultVO>();

        try {

            /*
             * 영화와 TV의 Provider 목록은 별도 API이기 때문에
             * 각각 Provider ID를 조회합니다.
             */
            String movieProviderIdText =
                    getProviderIdText(
                            API_TYPE_MOVIE,
                            platformList);

            String tvProviderIdText =
                    getProviderIdText(
                            API_TYPE_TV,
                            platformList);

            if (movieProviderIdText != null
                    && !movieProviderIdText.isBlank()) {

                List<SearchResultVO> movieList =
                        getMovieRankingList(
                                movieProviderIdText);

                combinedList.addAll(movieList);
            }

            if (tvProviderIdText != null
                    && !tvProviderIdText.isBlank()) {

                List<SearchResultVO> tvList =
                        getTvRankingList(
                                tvProviderIdText);

                combinedList.addAll(tvList);
            }

            /*
             * 같은 콘텐츠가 여러 페이지에 중복 포함되는 경우를 대비해
             * CONTENT_TYPE + TMDB_ID 기준으로 중복을 제거합니다.
             */
            combinedList =
                    removeDuplicateContent(combinedList);

            /*
             * 영화와 TV 결과를 TMDB popularity 기준으로
             * 다시 통합 정렬합니다.
             */
            Collections.sort(
                    combinedList,
                    new Comparator<SearchResultVO>() {

                        @Override
                        public int compare(
                                SearchResultVO first,
                                SearchResultVO second) {

                            double firstPopularity =
                                    first.getPopularity() == null
                                            ? 0.0
                                            : first.getPopularity();

                            double secondPopularity =
                                    second.getPopularity() == null
                                            ? 0.0
                                            : second.getPopularity();

                            return Double.compare(
                                    secondPopularity,
                                    firstPopularity);
                        }
                    });

            if (combinedList.size() > limit) {
                return new ArrayList<SearchResultVO>(
                        combinedList.subList(0, limit));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return combinedList;
    }

    private List<SearchResultVO> getMovieRankingList(
            String providerIdText) {

        List<SearchResultVO> movieList =
                new ArrayList<SearchResultVO>();

        for (int page = 1;
                page <= RANKING_API_PAGE_COUNT;
                page++) {

            try {

                String url =
                        tmdbApiBaseUrl
                        + "/discover/movie"
                        + "?language=" + tmdbApiLanguage
                        + "&region=KR"
                        + "&watch_region=KR"
                        + "&with_watch_monetization_types=flatrate"
                        + "&with_watch_providers=" + providerIdText
                        + "&sort_by=popularity.desc"
                        + "&include_adult=false"
                        + "&page=" + page;

                JsonNode root = callTmdbApi(url);

                JsonNode results = root.path("results");

                if (!results.isArray()) {
                    continue;
                }

                for (JsonNode movie : results) {

                    SearchResultVO vo =
                            createMovieRankingVO(movie);

                    if (vo != null) {
                        movieList.add(vo);
                    }
                }

            } catch (Exception e) {

                System.out.println(
                        "영화 인기 랭킹 조회 실패"
                        + " - page: "
                        + page);

                e.printStackTrace();
            }
        }

        return movieList;
    }

    private List<SearchResultVO> getTvRankingList(
            String providerIdText) {

        List<SearchResultVO> tvList =
                new ArrayList<SearchResultVO>();

        for (int page = 1;
                page <= RANKING_API_PAGE_COUNT;
                page++) {

            try {

                String url =
                        tmdbApiBaseUrl
                        + "/discover/tv"
                        + "?language=" + tmdbApiLanguage
                        + "&watch_region=KR"
                        + "&with_watch_monetization_types=flatrate"
                        + "&with_watch_providers=" + providerIdText
                        + "&sort_by=popularity.desc"
                        + "&include_adult=false"
                        + "&page=" + page;

                JsonNode root = callTmdbApi(url);

                JsonNode results = root.path("results");

                if (!results.isArray()) {
                    continue;
                }

                for (JsonNode tv : results) {

                    SearchResultVO vo =
                            createTvRankingVO(tv);

                    if (vo != null) {
                        tvList.add(vo);
                    }
                }

            } catch (Exception e) {

                System.out.println(
                        "TV 인기 랭킹 조회 실패"
                        + " - page: "
                        + page);

                e.printStackTrace();
            }
        }

        return tvList;
    }

    private SearchResultVO createMovieRankingVO(
            JsonNode movie) {

        Long tmdbId = movie.path("id").asLong();

        if (tmdbId == null || tmdbId == 0) {
            return null;
        }

        SearchResultVO vo = new SearchResultVO();

        vo.setTmdbId(tmdbId);
        vo.setContentType(CONTENT_TYPE_MOVIE);

        String title =
                movie.path("title").asText(null);

        String originalTitle =
                movie.path("original_title").asText(null);

        if (title == null || title.isBlank()) {
            title = originalTitle;
        }

        if (title == null || title.isBlank()) {
            return null;
        }

        vo.setTitle(title);
        vo.setOverview(
                movie.path("overview").asText(null));
        vo.setPosterPath(
                movie.path("poster_path").asText(null));
        vo.setReleaseDate(
                movie.path("release_date").asText(null));

        if (!movie.path("vote_average").isMissingNode()
                && !movie.path("vote_average").isNull()) {

            vo.setTmdbScore(
                    movie.path("vote_average").asDouble());
        }

        if (!movie.path("popularity").isMissingNode()
                && !movie.path("popularity").isNull()) {

            vo.setPopularity(
                    movie.path("popularity").asDouble());
        }

        return vo;
    }

    private SearchResultVO createTvRankingVO(
            JsonNode tv) {

        Long tmdbId = tv.path("id").asLong();

        if (tmdbId == null || tmdbId == 0) {
            return null;
        }

        SearchResultVO vo = new SearchResultVO();

        vo.setTmdbId(tmdbId);
        vo.setContentType(CONTENT_TYPE_TV);

        String title =
                tv.path("name").asText(null);

        String originalTitle =
                tv.path("original_name").asText(null);

        if (title == null || title.isBlank()) {
            title = originalTitle;
        }

        if (title == null || title.isBlank()) {
            return null;
        }

        vo.setTitle(title);
        vo.setOverview(
                tv.path("overview").asText(null));
        vo.setPosterPath(
                tv.path("poster_path").asText(null));
        vo.setReleaseDate(
                tv.path("first_air_date").asText(null));

        if (!tv.path("vote_average").isMissingNode()
                && !tv.path("vote_average").isNull()) {

            vo.setTmdbScore(
                    tv.path("vote_average").asDouble());
        }

        if (!tv.path("popularity").isMissingNode()
                && !tv.path("popularity").isNull()) {

            vo.setPopularity(
                    tv.path("popularity").asDouble());
        }

        return vo;
    }

    private List<SearchResultVO> removeDuplicateContent(
            List<SearchResultVO> originalList) {

        List<SearchResultVO> resultList =
                new ArrayList<SearchResultVO>();

        Set<String> contentKeySet =
                new LinkedHashSet<String>();

        for (SearchResultVO content : originalList) {

            if (content.getTmdbId() == null
                    || content.getContentType() == null) {

                continue;
            }

            String contentKey =
                    content.getContentType()
                    + ":"
                    + content.getTmdbId();

            if (contentKeySet.contains(contentKey)) {
                continue;
            }

            contentKeySet.add(contentKey);
            resultList.add(content);
        }

        return resultList;
    }

    private String getProviderIdText(
            String apiType,
            List<String> selectedPlatformList) {

        Set<String> providerIdSet =
                new LinkedHashSet<String>();

        try {

            String url =
                    tmdbApiBaseUrl
                    + "/watch/providers/"
                    + apiType
                    + "?language="
                    + tmdbApiLanguage
                    + "&watch_region=KR";

            JsonNode root = callTmdbApi(url);

            JsonNode results = root.path("results");

            if (!results.isArray()) {
                return "";
            }

            Iterator<JsonNode> iterator =
                    results.iterator();

            while (iterator.hasNext()) {

                JsonNode provider = iterator.next();

                String tmdbProviderName =
                        provider.path("provider_name")
                                .asText(null);

                int providerId =
                        provider.path("provider_id")
                                .asInt();

                String platformName =
                        convertTmdbProviderName(
                                tmdbProviderName);

                if (platformName == null) {
                    continue;
                }

                if (selectedPlatformList != null
                        && !selectedPlatformList.isEmpty()
                        && !selectedPlatformList.contains(
                                platformName)) {

                    continue;
                }

                if (providerId > 0) {
                    providerIdSet.add(
                            String.valueOf(providerId));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
         * 파이프는 TMDB Discover API에서
         * Provider OR 조건으로 사용됩니다.
         */
        return String.join("|", providerIdSet);
    }

    private String convertTmdbProviderName(
            String tmdbProviderName) {

        if (tmdbProviderName == null) {
            return null;
        }

        String originalName =
                tmdbProviderName.trim();

        String normalizedName =
                originalName.toLowerCase()
                        .replace(" ", "")
                        .replace("_", "")
                        .replace("-", "")
                        .replace("+", "");

        if (normalizedName.contains("netflix")
                || originalName.contains("넷플릭스")) {

            return "Netflix";
        }

        if (normalizedName.contains("tving")
                || originalName.contains("티빙")) {

            return "TVING";
        }

        if (normalizedName.contains("wavve")
                || originalName.contains("웨이브")) {

            return "wavve";
        }

        if (normalizedName.contains("disney")
                || originalName.contains("디즈니")) {

            return "Disney Plus";
        }

        if (normalizedName.contains("watcha")
                || originalName.contains("왓챠")) {

            return "Watcha";
        }

        return null;
    }

    private String normalizePlatformName(
            String platformName) {

        if (platformName == null
                || platformName.trim().isEmpty()) {

            return null;
        }

        String name = platformName.trim();

        if ("Netflix".equalsIgnoreCase(name)
                || "넷플릭스".equals(name)) {

            return "Netflix";
        }

        if ("TVING".equalsIgnoreCase(name)
                || "티빙".equals(name)) {

            return "TVING";
        }

        if ("wavve".equalsIgnoreCase(name)
                || "웨이브".equals(name)) {

            return "wavve";
        }

        if ("Disney Plus".equalsIgnoreCase(name)
                || "Disney+".equalsIgnoreCase(name)
                || "DisneyPlus".equalsIgnoreCase(name)
                || "디즈니플러스".equals(name)
                || "디즈니+".equals(name)) {

            return "Disney Plus";
        }

        if ("Watcha".equalsIgnoreCase(name)
                || "왓챠".equals(name)) {

            return "Watcha";
        }

        return null;
    }

    private int normalizeLimit(int limit) {

        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }

        if (limit > MAX_LIMIT) {
            return MAX_LIMIT;
        }

        return limit;
    }

    private JsonNode callTmdbApi(String url) {

        try {

            RestTemplate restTemplate =
                    new RestTemplate();

            ObjectMapper objectMapper =
                    new ObjectMapper();

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setBearerAuth(token);

            HttpEntity<String> entity =
                    new HttpEntity<String>(headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            entity,
                            String.class);

            if (response.getBody() == null
                    || response.getBody().isBlank()) {

                throw new IllegalStateException(
                        "TMDB API 응답 본문이 없습니다.");
            }

            return objectMapper.readTree(
                    response.getBody());

        } catch (Exception e) {

            throw new IllegalStateException(
                    "TMDB API 호출에 실패했습니다. URL="
                    + url,
                    e);
        }
    }
}