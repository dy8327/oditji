package com.project.oditji.tmdb.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.project.oditji.tmdb.dao.TmdbDAO;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** 상세 API URL 생성에서 append_to_response를 사용하지 않는 양쪽 분기를 보완합니다. */
class TmdbServiceImplDetailRootNoCreditsCoverageTest {

    private TmdbServiceImpl service;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);

        service = new TmdbServiceImpl(
                mock(TmdbDAO.class),
                JsonMapper.builder().build());

        ReflectionTestUtils.setField(service, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(service, "token", "test-token");
        ReflectionTestUtils.setField(service, "tmdbApiBaseUrl", "https://tmdb.test/3");
        ReflectionTestUtils.setField(service, "tmdbApiLanguage", "ko-KR");
        ReflectionTestUtils.setField(service, "tmdbApiRegion", "KR");
    }

    @Test
    void detailRootShouldOmitCreditsForMovieAndTvWhenRequested() {
        expectJson("https://tmdb.test/3/movie/100?language=ko-KR");
        expectJson("https://tmdb.test/3/tv/200?language=ko-KR");

        JsonNode movie = ReflectionTestUtils.invokeMethod(
                service,
                "getDetailRoot",
                100L,
                "MOVIE",
                false);

        JsonNode tv = ReflectionTestUtils.invokeMethod(
                service,
                "getDetailRoot",
                200L,
                "TV",
                false);

        assertTrue(movie.isObject());
        assertTrue(tv.isObject());
        server.verify();
    }

    private void expectJson(String url) {
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
    }
}
