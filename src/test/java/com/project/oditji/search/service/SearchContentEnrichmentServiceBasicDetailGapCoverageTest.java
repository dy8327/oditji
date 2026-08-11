package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** 검색 상세 보강의 영화/TV 기본정보 채우기와 파서 잔여 분기를 보완합니다. */
class SearchContentEnrichmentServiceBasicDetailGapCoverageTest {

    private SearchContentEnrichmentService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentEnrichmentService(
                mock(TmdbApiClient.class),
                mock(SearchContentManualOverrideService.class),
                mock(SearchContentAgeRatingResolver.class));
    }

    @Test
    void movieBasicDetailShouldFillOnlyMissingFields() {
        JSONObject detail = new JSONObject()
                .put("title", "영화")
                .put("original_title", "Movie")
                .put("release_date", "2026-08-11")
                .put("runtime", 120)
                .put("poster_path", "/poster.jpg")
                .put("popularity", 55.5);

        CachedContentVO movie = new CachedContentVO();
        movie.setContentType("MOVIE");

        ReflectionTestUtils.invokeMethod(service, "fillBasicContentDetail", movie, detail);

        assertEquals("영화", movie.getTitle());
        assertEquals("Movie", movie.getOriginalTitle());
        assertEquals("2026-08-11", movie.getReleaseDate());
        assertEquals(Integer.valueOf(120), movie.getRuntime());
        assertEquals("/poster.jpg", movie.getPosterPath());
        assertEquals(55.5, movie.getPopularity());

        movie.setTitle("기존제목");
        movie.setOriginalTitle("기존원제");
        movie.setReleaseDate("2020-01-01");
        movie.setRuntime(90);
        movie.setPosterPath("/old.jpg");
        movie.setPopularity(1.0);

        ReflectionTestUtils.invokeMethod(service, "fillBasicContentDetail", movie, detail);
        assertEquals("기존제목", movie.getTitle());
        assertEquals(Integer.valueOf(90), movie.getRuntime());
        assertEquals("/old.jpg", movie.getPosterPath());
    }

    @Test
    void tvBasicDetailShouldPreferLatestEpisodeAirDateAndFirstPositiveRuntime() {
        JSONObject detail = new JSONObject()
                .put("name", "드라마")
                .put("original_name", "Drama")
                .put("first_air_date", "2026-01-01")
                .put("last_episode_to_air", new JSONObject().put("air_date", "2026-08-10"))
                .put("last_air_date", "2026-08-01")
                .put("episode_run_time", new JSONArray().put(0).put(-1).put(55));

        CachedContentVO tv = new CachedContentVO();
        tv.setContentType("TV");

        ReflectionTestUtils.invokeMethod(service, "fillBasicContentDetail", tv, detail);

        assertEquals("드라마", tv.getTitle());
        assertEquals("Drama", tv.getOriginalTitle());
        assertEquals("2026-01-01", tv.getReleaseDate());
        assertEquals("2026-08-10", tv.getLastAirDate());
        assertEquals(Integer.valueOf(55), tv.getRuntime());

        JSONObject fallback = new JSONObject().put("last_air_date", "2026-07-31");
        assertEquals(
                "2026-07-31",
                ReflectionTestUtils.invokeMethod(service, "resolveTvLastAirDate", fallback));
        assertNull(ReflectionTestUtils.invokeMethod(service, "resolveTvLastAirDate", (Object) null));
    }

    @Test
    void peopleAndGenreParsersShouldIgnoreInvalidDuplicateAndExcessEntries() {
        JSONArray genres = new JSONArray()
                .put(JSONObject.NULL)
                .put(new JSONObject().put("name", " "))
                .put(new JSONObject().put("name", "액션"))
                .put(new JSONObject().put("name", "액션"))
                .put(new JSONObject().put("name", "드라마"));
        assertEquals("액션, 드라마", ReflectionTestUtils.invokeMethod(service, "parseGenreNames", genres));

        JSONArray crew = new JSONArray()
                .put(JSONObject.NULL)
                .put(new JSONObject().put("job", "Writer").put("name", "작가"))
                .put(new JSONObject().put("job", "Director").put("name", "감독"))
                .put(new JSONObject().put("job", "director").put("name", "감독"));
        assertEquals("감독", ReflectionTestUtils.invokeMethod(service, "parseMovieDirector", crew));

        JSONArray creators = new JSONArray()
                .put(JSONObject.NULL)
                .put(new JSONObject().put("name", " "))
                .put(new JSONObject().put("name", "Creator"))
                .put(new JSONObject().put("name", "Creator"));
        assertEquals("Creator", ReflectionTestUtils.invokeMethod(service, "parseTvCreator", creators));

        JSONArray cast = new JSONArray();
        for (int index = 1; index <= 7; index++) {
            cast.put(new JSONObject().put("name", "배우" + index));
        }
        assertEquals(
                "배우1, 배우2, 배우3, 배우4, 배우5",
                ReflectionTestUtils.invokeMethod(service, "parseCastNames", cast));
    }
}
