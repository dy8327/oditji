package com.project.oditji.common.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.dao.TmdbDAO;
import com.project.oditji.tmdb.vo.OttPlatformVO;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Service
public class MainContentPlatformService {

    private final TmdbDAO tmdbDAO;
    private final JsonMapper jsonMapper;
    private final RestTemplate restTemplate;

    /*
     * 같은 콘텐츠가 메인 페이지에 반복 표시될 때
     * watch/providers API를 매번 다시 호출하지 않도록 캐시한다.
     *
     * 캐시 키 예시:
     * MOVIE:12345
     * TV:67890
     */
    private final Map<String, List<OttPlatformVO>> platformCache =
            new ConcurrentHashMap<String, List<OttPlatformVO>>();

    @Value("${tmdb.api.token}")
    private String token;

    @Value("${tmdb.api.base-url:https://api.themoviedb.org/3}")
    private String tmdbApiBaseUrl;

    @Value("${tmdb.api.region:KR}")
    private String tmdbApiRegion;

    public MainContentPlatformService(
            TmdbDAO tmdbDAO,
            JsonMapper jsonMapper) {

        this.tmdbDAO = tmdbDAO;
        this.jsonMapper = jsonMapper;
        this.restTemplate = new RestTemplate();
    }

    /**
     * 메인 콘텐츠 카드에 한국 기준 정액제 OTT 로고를 연결한다.
     *
     * selectedPlatformNames가 비어 있으면
     * ODITJI 지원 OTT 전체를 표시한다.
     *
     * selectedPlatformNames가 있으면
     * 회원이 선택한 OTT와 실제 제공 OTT의 교집합만 표시한다.
     *
     * @param contentList
     *        메인에 출력할 콘텐츠 목록
     *
     * @param selectedPlatformNames
     *        회원이 선택한 OTT 이름 목록
     */
    public void attachPlatformLogos(
            List<SearchResultVO> contentList,
            List<String> selectedPlatformNames) {

        if (contentList == null || contentList.isEmpty()) {
            return;
        }

        /*
         * OTT_PLATFORM 테이블에서 활성 플랫폼 목록을 가져온다.
         */
        Map<String, OttPlatformVO> activePlatformMap =
                createActivePlatformMap();

        if (activePlatformMap.isEmpty()) {
            return;
        }

        /*
         * 회원이 선택한 OTT 이름을
         * Netflix → netflix 같은 내부 비교 키로 변환한다.
         */
        Set<String> selectedPlatformKeys =
                createSelectedPlatformKeys(
                        selectedPlatformNames
                );

        for (SearchResultVO content : contentList) {

            if (content == null
                    || content.getTmdbId() == null
                    || content.getContentType() == null) {
                continue;
            }

            /*
             * TMDB 한국 watch/providers에서
             * 정액제 OTT 목록을 조회한다.
             */
            List<OttPlatformVO> allPlatformList =
                    getCachedPlatformList(
                            content.getTmdbId(),
                            content.getContentType(),
                            activePlatformMap
                    );

            /*
             * 추천 콘텐츠인 경우
             * 회원 선택 OTT와 실제 제공 OTT의 교집합만 남긴다.
             *
             * 오늘의 콘텐츠처럼 selectedPlatformKeys가 비어 있으면
             * 지원 OTT 전체를 그대로 사용한다.
             */
            content.setPlatformList(
                    filterSelectedPlatforms(
                            allPlatformList,
                            selectedPlatformKeys
                    )
            );
        }
    }

    /**
     * DB의 활성 OTT 플랫폼을
     * 플랫폼 정규화 키 기준 Map으로 변환한다.
     */
    private Map<String, OttPlatformVO> createActivePlatformMap() {

        Map<String, OttPlatformVO> platformMap =
                new HashMap<String, OttPlatformVO>();

        List<OttPlatformVO> activePlatformList =
                tmdbDAO.selectActivePlatformList();

        if (activePlatformList == null) {
            return platformMap;
        }

        for (OttPlatformVO platform : activePlatformList) {

            if (platform == null
                    || platform.getPlatformName() == null) {
                continue;
            }

            String key = normalizePlatformName(
                    platform.getPlatformName()
            );

            if (!key.isEmpty()) {
                platformMap.put(key, platform);
            }
        }

        return platformMap;
    }

    /**
     * 회원 선택 OTT 이름을 정규화한다.
     */
    private Set<String> createSelectedPlatformKeys(
            List<String> selectedPlatformNames) {

        Set<String> selectedKeys =
                new LinkedHashSet<String>();

        if (selectedPlatformNames == null) {
            return selectedKeys;
        }

        for (String platformName : selectedPlatformNames) {

            String key =
                    normalizePlatformName(platformName);

            if (!key.isEmpty()) {
                selectedKeys.add(key);
            }
        }

        return selectedKeys;
    }

    /**
     * 콘텐츠별 OTT 제공처 캐시 조회
     */
    private List<OttPlatformVO> getCachedPlatformList(
            Long tmdbId,
            String contentType,
            Map<String, OttPlatformVO> activePlatformMap) {

        String cacheKey =
                contentType
                        .trim()
                        .toUpperCase(Locale.ROOT)
                + ":"
                + tmdbId;

        List<OttPlatformVO> cachedList =
                platformCache.get(cacheKey);

        if (cachedList != null) {
            return new ArrayList<OttPlatformVO>(
                    cachedList
            );
        }

        List<OttPlatformVO> loadedList =
                findPlatformList(
                        tmdbId,
                        contentType,
                        activePlatformMap
                );

        platformCache.put(
                cacheKey,
                new ArrayList<OttPlatformVO>(
                        loadedList
                )
        );

        return loadedList;
    }

    /**
     * TMDB watch/providers API에서
     * 한국 기준 정액제 제공처를 조회한다.
     *
     * rent, buy는 포함하지 않는다.
     */
    private List<OttPlatformVO> findPlatformList(
            Long tmdbId,
            String contentType,
            Map<String, OttPlatformVO> activePlatformMap) {

        List<OttPlatformVO> resultList =
                new ArrayList<OttPlatformVO>();

        String apiType =
                "MOVIE".equalsIgnoreCase(contentType)
                        ? "movie"
                        : "tv";

        String url = tmdbApiBaseUrl
                + "/"
                + apiType
                + "/"
                + tmdbId
                + "/watch/providers";

        JsonNode flatrate =
                callTmdbApi(url)
                        .path("results")
                        .path(tmdbApiRegion)
                        .path("flatrate");

        if (!flatrate.isArray()) {
            return resultList;
        }

        Set<Integer> duplicatePlatformNos =
                new HashSet<Integer>();

        for (JsonNode provider : flatrate) {

            String providerName =
                    provider
                            .path("provider_name")
                            .asString(null);

            String providerKey =
                    normalizePlatformName(
                            providerName
                    );

            /*
             * TMDB 제공처와
             * DB의 활성 ODITJI 플랫폼을 매칭한다.
             */
            OttPlatformVO platform =
                    activePlatformMap.get(
                            providerKey
                    );

            if (platform == null) {
                continue;
            }

            Integer platformNo =
                    platform.getPlatformNo();

            /*
             * 동일 플랫폼 중복 방지
             */
            if (platformNo == null
                    || duplicatePlatformNos.add(
                            platformNo
                    )) {

                resultList.add(platform);
            }
        }

        return resultList;
    }

    /**
     * 회원 선택 OTT가 있다면 해당 OTT만 남긴다.
     *
     * 선택 OTT가 없으면 전체 플랫폼 목록을 반환한다.
     */
    private List<OttPlatformVO> filterSelectedPlatforms(
            List<OttPlatformVO> platformList,
            Set<String> selectedPlatformKeys) {

        List<OttPlatformVO> resultList =
                new ArrayList<OttPlatformVO>();

        if (platformList == null
                || platformList.isEmpty()) {
            return resultList;
        }

        /*
         * 비로그인 또는 OTT 미선택 상태
         */
        if (selectedPlatformKeys == null
                || selectedPlatformKeys.isEmpty()) {

            resultList.addAll(platformList);

            return resultList;
        }

        for (OttPlatformVO platform : platformList) {

            if (platform == null
                    || platform.getPlatformName() == null) {
                continue;
            }

            String key =
                    normalizePlatformName(
                            platform.getPlatformName()
                    );

            if (selectedPlatformKeys.contains(key)) {
                resultList.add(platform);
            }
        }

        return resultList;
    }

    /**
     * TMDB와 DB에서 플랫폼 이름 표기가 달라도
     * 동일 플랫폼으로 매칭되도록 정규화한다.
     *
     * 예:
     * Coupang Play
     * Coupangplay
     * coupang_play
     * → coupang
     */
    private String normalizePlatformName(
            String platformName) {

        if (platformName == null) {
            return "";
        }

        String normalized =
                platformName
                        .trim()
                        .toLowerCase(Locale.ROOT)
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

    /**
     * TMDB API 공통 호출
     */
    private JsonNode callTmdbApi(String url) {

        try {

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setBearerAuth(token);

            HttpEntity<String> entity =
                    new HttpEntity<String>(
                            headers
                    );

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            entity,
                            String.class
                    );

            return jsonMapper.readTree(
                    response.getBody()
            );

        } catch (Exception e) {

            /*
             * OTT 로고 조회 실패 때문에
             * 메인 페이지 전체가 오류 화면으로 이동하지 않도록
             * 빈 JSON 객체를 반환한다.
             */
            System.err.println(
                    "메인 OTT 제공처 조회 실패: "
                            + url
                            + " / "
                            + e.getMessage()
            );

            return jsonMapper.createObjectNode();
        }
    }
}