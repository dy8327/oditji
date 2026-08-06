package com.project.oditji.tmdb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.project.oditji.tmdb.dao.TmdbDAO;

import tools.jackson.databind.json.JsonMapper;

/** TMDB 전체 적재 오케스트레이션과 기본 목록 반복 호출을 Mock HTTP 서버로 검증합니다. */
@ExtendWith(MockitoExtension.class)
class TmdbServiceImplOrchestrationCoverageTest {

    @Mock
    private TmdbDAO tmdbDAO;

    private TmdbServiceImpl service;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        service = new TmdbServiceImpl(
                tmdbDAO,
                JsonMapper.builder().build());

        ReflectionTestUtils.setField(service, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(service, "token", "test-token");
        ReflectionTestUtils.setField(service, "tmdbApiBaseUrl", "https://tmdb.test/3");
        ReflectionTestUtils.setField(service, "tmdbApiLanguage", "ko-KR");
        ReflectionTestUtils.setField(service, "tmdbApiRegion", "KR");
    }

    @Test
    void loadAllDataShouldCompleteWhenNoSupportedProvidersOrSavedContentExist() {
        when(tmdbDAO.selectContentList()).thenReturn(List.of());
        expectJson(
                "https://tmdb.test/3/watch/providers/movie?language=ko-KR&watch_region=KR",
                "{\"results\":[]}");
        expectJson(
                "https://tmdb.test/3/watch/providers/tv?language=ko-KR&watch_region=KR",
                "{\"results\":[]}");

        assertEquals(0, service.loadAllData());

        verify(tmdbDAO, times(4)).selectContentList();
        server.verify();
    }

    @Test
    void movieBasicLoadShouldRequestConfiguredPagesAndSkipEmptyResults() {
        expectJson(
                "https://tmdb.test/3/watch/providers/movie?language=ko-KR&watch_region=KR",
                """
                {"results":[
                  {"provider_id":8,"provider_name":"Netflix"},
                  {"provider_id":337,"provider_name":"Disney Plus"},
                  {"provider_id":999,"provider_name":"지원하지 않음"}
                ]}
                """);

        for (int page = 1; page <= 10; page++) {
            expectJson(
                    "https://tmdb.test/3/discover/movie"
                            + "?language=ko-KR"
                            + "&region=KR"
                            + "&watch_region=KR"
                            + "&include_adult=false"
                            + "&include_video=false"
                            + "&with_watch_monetization_types=flatrate"
                            + "&with_watch_providers=8%7C337"
                            + "&sort_by=popularity.desc"
                            + "&page=" + page,
                    "{\"results\":[]}");
        }

        assertEquals(0, service.loadMovieData());
        server.verify();
    }

    @Test
    void tvBasicLoadShouldRequestFifteenPages() {
        expectJson(
                "https://tmdb.test/3/watch/providers/tv?language=ko-KR&watch_region=KR",
                "{\"results\":[{\"provider_id\":8,\"provider_name\":\"Netflix\"}]}");

        for (int page = 1; page <= 15; page++) {
            expectJson(
                    "https://tmdb.test/3/discover/tv"
                            + "?language=ko-KR"
                            + "&region=KR"
                            + "&watch_region=KR"
                            + "&include_adult=false"
                            + "&with_watch_monetization_types=flatrate"
                            + "&with_watch_providers=8"
                            + "&sort_by=popularity.desc"
                            + "&page=" + page,
                    "{\"results\":[]}");
        }

        assertEquals(0, service.loadTvData());
        server.verify();
    }

    private void expectJson(String url, String responseBody) {
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));
    }
}
