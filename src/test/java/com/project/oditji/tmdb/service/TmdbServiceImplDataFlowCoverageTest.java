package com.project.oditji.tmdb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.tmdb.dao.TmdbDAO;
import com.project.oditji.tmdb.vo.ActorVO;
import com.project.oditji.tmdb.vo.DirectorVO;
import com.project.oditji.tmdb.vo.TmdbVO;

import tools.jackson.databind.json.JsonMapper;

/** TMDB 상세·플랫폼·인물 저장의 실제 데이터 흐름을 Mock HTTP 서버로 검증합니다. */
@ExtendWith(MockitoExtension.class)
class TmdbServiceImplDataFlowCoverageTest {

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
    void movieDetailUpdateShouldFillFieldsAndSaveNewActorAndDirector() {
        TmdbVO movie = tmdb(100L, "MOVIE");
        TmdbVO tv = tmdb(200L, "TV");
        when(tmdbDAO.selectContentList()).thenReturn(List.of(movie, tv));
        when(tmdbDAO.findContentNo(100L, "MOVIE")).thenReturn(10);
        when(tmdbDAO.findActorNoByTmdbId(1L)).thenReturn(null, 101);
        when(tmdbDAO.existsContentActor(10, 101)).thenReturn(0);
        when(tmdbDAO.findDirectorNoByTmdbId(2L)).thenReturn(null, 201);
        when(tmdbDAO.existsContentDirector(10, 201)).thenReturn(0);

        expectJson(
                "https://tmdb.test/3/movie/100?language=ko-KR&append_to_response=credits,release_dates",
                """
                {
                  "genres":[{"name":"드라마"}],
                  "runtime":123,
                  "credits":{
                    "cast":[{"id":1,"name":"배우 A","profile_path":"/actor.jpg","character":"주인공"}],
                    "crew":[
                      {"id":2,"name":"감독 A","profile_path":"/director.jpg","job":"Director"},
                      {"id":3,"name":"작가 A","job":"Writer"}
                    ]
                  },
                  "release_dates":{"results":[{"iso_3166_1":"KR","release_dates":[{"certification":"15"}]}]}
                }
                """);

        assertEquals(1, service.updateMovieDetailData());
        assertEquals(123, movie.getRuntime());
        assertNull(movie.getEpisodeCount());
        assertEquals("감독 A", movie.getDirector());
        assertEquals("배우 A", movie.getCastNames());
        assertEquals("15", movie.getAgeRating());

        ArgumentCaptor<ActorVO> actorCaptor = ArgumentCaptor.forClass(ActorVO.class);
        verify(tmdbDAO).insertActor(actorCaptor.capture());
        assertEquals("배우 A", actorCaptor.getValue().getActorName());
        verify(tmdbDAO).insertContentActor(10, 101, "주인공", 1);

        ArgumentCaptor<DirectorVO> directorCaptor = ArgumentCaptor.forClass(DirectorVO.class);
        verify(tmdbDAO).insertDirector(directorCaptor.capture());
        assertEquals("감독 A", directorCaptor.getValue().getDirectorName());
        verify(tmdbDAO).insertContentDirector(10, 201, "DIRECTOR", 1);
        server.verify();
    }

    @Test
    void tvDetailUpdateShouldSaveCreatorAndExistingPeopleWithoutDuplicates() {
        TmdbVO tv = tmdb(200L, "TV");
        when(tmdbDAO.selectContentList()).thenReturn(List.of(tv));
        when(tmdbDAO.findContentNo(200L, "TV")).thenReturn(20);
        when(tmdbDAO.findActorNoByTmdbId(11L)).thenReturn(111);
        when(tmdbDAO.existsContentActor(20, 111)).thenReturn(1);
        when(tmdbDAO.findDirectorNoByTmdbId(12L)).thenReturn(212);
        when(tmdbDAO.existsContentDirector(20, 212)).thenReturn(0);
        when(tmdbDAO.findDirectorNoByTmdbId(13L)).thenReturn(213);
        when(tmdbDAO.existsContentDirector(20, 213)).thenReturn(1);

        expectJson(
                "https://tmdb.test/3/tv/200?language=ko-KR&append_to_response=credits,content_ratings",
                """
                {
                  "genres":[{"name":"코미디"}],
                  "episode_run_time":[55],
                  "number_of_episodes":16,
                  "created_by":[{"id":12,"name":"크리에이터","profile_path":"/creator.jpg"}],
                  "credits":{
                    "cast":[{"id":11,"name":"배우 B","character":"역할 B"}],
                    "crew":[{"id":13,"name":"연출 B","job":"Director"}]
                  },
                  "content_ratings":{"results":[{"iso_3166_1":"US","rating":"TV-14"}]}
                }
                """);

        assertEquals(1, service.updateTvDetailData());
        assertEquals(55, tv.getRuntime());
        assertEquals(16, tv.getEpisodeCount());
        assertEquals("크리에이터", tv.getDirector());
        assertEquals("15", tv.getAgeRating());
        verify(tmdbDAO, never()).insertActor(any(ActorVO.class));
        verify(tmdbDAO).insertContentDirector(20, 212, "CREATOR", 1);
        verify(tmdbDAO, never()).insertContentDirector(20, 213, "DIRECTOR", 1);
        server.verify();
    }

    @Test
    void platformLoadShouldInsertOnlySupportedMissingMovieAndTvRelations() {
        TmdbVO movie = tmdb(300L, "MOVIE");
        TmdbVO tv = tmdb(400L, "TV");
        when(tmdbDAO.selectContentList()).thenReturn(List.of(movie, tv));
        when(tmdbDAO.findContentNo(300L, "MOVIE")).thenReturn(30);
        when(tmdbDAO.findPlatformNo("Netflix")).thenReturn(1);
        when(tmdbDAO.findPlatformNo("Wavve")).thenReturn(2);
        when(tmdbDAO.existsContentPlatform(30, 1)).thenReturn(1);
        when(tmdbDAO.existsContentPlatform(30, 2)).thenReturn(0);

        expectJson(
                "https://tmdb.test/3/movie/300/watch/providers",
                """
                {"results":{"KR":{"flatrate":[
                  {"provider_name":"Netflix"},
                  {"provider_name":"Wavve"},
                  {"provider_name":"Other"}
                ]}}}
                """);

        assertEquals(1, service.loadMoviePlatformData());
        verify(tmdbDAO, never()).insertContentPlatform(30, 1);
        verify(tmdbDAO).insertContentPlatform(30, 2);
        server.verify();
    }

    @Test
    void detailForSaveAndActorPreviewShouldMapMovieTvAndLimitValidCast() {
        expectJson(
                "https://tmdb.test/3/movie/500?language=ko-KR&append_to_response=credits,release_dates",
                movieDetailJson());
        expectJson(
                "https://tmdb.test/3/tv/600?language=ko-KR&append_to_response=credits,content_ratings",
                tvDetailJson());
        expectJson(
                "https://tmdb.test/3/movie/700?language=ko-KR&append_to_response=credits,release_dates",
                actorPreviewJson());

        ContentVO movie = service.getDetailForSave(500L, " movie ");
        ContentVO tv = service.getDetailForSave(600L, "tv");
        List<ActorVO> actors = service.getContentActorPreview(700L, " movie ");

        assertEquals(500L, movie.getTmdbId());
        assertEquals("MOVIE", movie.getContentType());
        assertEquals("영화 제목", movie.getTitle());
        assertEquals(LocalDate.of(2026, Month.AUGUST, 1), movie.getReleaseDate());
        assertEquals(120, movie.getRuntime());
        assertEquals("ALL", movie.getAgeRating());

        assertEquals(600L, tv.getTmdbId());
        assertEquals("TV", tv.getContentType());
        assertEquals(12, tv.getEpisodeCount());
        assertEquals("12", tv.getAgeRating());

        assertEquals(5, actors.size());
        assertEquals(1, actors.get(0).getDisplayOrder());
        assertEquals(5, actors.get(4).getDisplayOrder());
        assertTrue(actors.stream().allMatch(actor -> actor.getActorName().startsWith("배우")));
        server.verify();
    }

    private TmdbVO tmdb(Long tmdbId, String contentType) {
        TmdbVO vo = new TmdbVO();
        vo.setTmdbId(tmdbId);
        vo.setContentType(contentType);
        return vo;
    }

    private void expectJson(String url, String responseBody) {
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));
    }

    private String movieDetailJson() {
        return """
                {
                  "id":500,
                  "title":"영화 제목",
                  "original_title":"Movie Title",
                  "overview":"줄거리",
                  "poster_path":"/poster.jpg",
                  "backdrop_path":"/backdrop.jpg",
                  "release_date":"2026-08-01",
                  "genres":[{"name":"드라마"}],
                  "vote_average":8.5,
                  "runtime":120,
                  "credits":{"cast":[{"id":1,"name":"배우 A"}],"crew":[{"id":2,"name":"감독 A","job":"Director"}]},
                  "release_dates":{"results":[{"iso_3166_1":"KR","release_dates":[{"certification":"전체"}]}]}
                }
                """;
    }

    private String tvDetailJson() {
        return """
                {
                  "id":600,
                  "name":"드라마 제목",
                  "original_name":"Drama Title",
                  "overview":"드라마 줄거리",
                  "poster_path":"/tv.jpg",
                  "backdrop_path":"/tv-backdrop.jpg",
                  "first_air_date":"2026-08-02",
                  "genres":[{"name":"코미디"}],
                  "vote_average":7.5,
                  "episode_run_time":[50],
                  "number_of_episodes":12,
                  "created_by":[{"id":3,"name":"크리에이터"}],
                  "credits":{"cast":[{"id":4,"name":"배우 B"}],"crew":[]},
                  "content_ratings":{"results":[{"iso_3166_1":"KR","rating":"12세"}]}
                }
                """;
    }

    private String actorPreviewJson() {
        return """
                {"credits":{"cast":[
                  {"id":0,"name":"무효"},
                  {"id":1,"name":"배우1","profile_path":"/1.jpg","character":"역할1"},
                  {"id":2,"name":"배우2","profile_path":"/2.jpg","character":"역할2"},
                  {"id":3,"name":"배우3","profile_path":"/3.jpg","character":"역할3"},
                  {"id":4,"name":"배우4","profile_path":"/4.jpg","character":"역할4"},
                  {"id":5,"name":"배우5","profile_path":"/5.jpg","character":"역할5"},
                  {"id":6,"name":"배우6","profile_path":"/6.jpg","character":"역할6"}
                ]}}
                """;
    }
}
