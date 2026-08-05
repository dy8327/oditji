package com.project.oditji.tmdb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.Month;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.content.vo.FilmographyVO;
import com.project.oditji.tmdb.dao.TmdbDAO;
import com.project.oditji.tmdb.vo.TmdbVO;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** TMDB 상세 JSON 파싱, 등급 변환, 차단 정책과 필모그래피 정리 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class TmdbServiceImplParsingCoverageTest {

    @Mock
    private TmdbDAO tmdbDAO;

    private JsonMapper jsonMapper;
    private TmdbServiceImpl service;

    @BeforeEach
    void setUp() {
        jsonMapper = JsonMapper.builder().build();
        service = new TmdbServiceImpl(tmdbDAO, jsonMapper);
    }

    @Test
    void basicMovieAndTvItemsShouldMapTitlesDatesGenresAndScores() throws Exception {
        JsonNode movie = json("{"
                + "\"id\":10,\"title\":\"영화\",\"original_title\":\"Movie\","
                + "\"release_date\":\"2026-08-01\",\"overview\":\"줄거리\","
                + "\"poster_path\":\"/poster.jpg\",\"backdrop_path\":\"/back.jpg\","
                + "\"genre_ids\":[28,18,28,9999],\"vote_average\":8.7}");
        TmdbVO movieVo = ReflectionTestUtils.invokeMethod(
                service,
                "createBasicTmdbVO",
                movie,
                "MOVIE");
        assertEquals(10L, movieVo.getTmdbId());
        assertEquals("영화", movieVo.getTitle());
        assertEquals("Movie", movieVo.getOriginalTitle());
        assertEquals(LocalDate.of(2026, Month.AUGUST, 1), movieVo.getReleaseDate());
        assertEquals("액션, 드라마", movieVo.getGenreText());
        assertEquals(8.7, movieVo.getTmdbScore());

        JsonNode tv = json("{"
                + "\"id\":20,\"name\":\" \",\"original_name\":\"TV Original\","
                + "\"first_air_date\":\"invalid\",\"genre_ids\":[10759,10765],"
                + "\"vote_average\":null}");
        TmdbVO tvVo = ReflectionTestUtils.invokeMethod(
                service,
                "createBasicTmdbVO",
                tv,
                "TV");
        assertEquals("TV Original", tvVo.getTitle());
        assertNull(tvVo.getReleaseDate());
        assertEquals("액션·모험, SF·판타지", tvVo.getGenreText());
        assertNull(tvVo.getTmdbScore());
    }

    @Test
    void detailFillersShouldParsePeopleRuntimeEpisodeAndRatings() throws Exception {
        JsonNode movieRoot = json("{"
                + "\"genres\":[{\"name\":\"액션\"},{\"name\":\"드라마\"}],"
                + "\"runtime\":120,"
                + "\"credits\":{"
                + "\"crew\":[{\"job\":\"Director\",\"name\":\"감독A\"},"
                + "{\"job\":\"Writer\",\"name\":\"작가\"},"
                + "{\"job\":\"Director\",\"name\":\"감독A\"}],"
                + "\"cast\":[{\"name\":\"배우1\"},{\"name\":\"배우2\"},"
                + "{\"name\":\"배우3\"},{\"name\":\"배우4\"},"
                + "{\"name\":\"배우5\"},{\"name\":\"배우6\"}]},"
                + "\"release_dates\":{\"results\":["
                + "{\"iso_3166_1\":\"US\",\"release_dates\":[{\"certification\":\"R\"}]},"
                + "{\"iso_3166_1\":\"KR\",\"release_dates\":[{\"certification\":\"15세\"}]}]}}");
        TmdbVO movieVo = new TmdbVO();
        ReflectionTestUtils.invokeMethod(service, "fillMovieDetail", movieVo, movieRoot);
        assertEquals("액션, 드라마", movieVo.getGenreText());
        assertEquals(120, movieVo.getRuntime());
        assertNull(movieVo.getEpisodeCount());
        assertEquals("감독A", movieVo.getDirector());
        assertEquals("배우1, 배우2, 배우3, 배우4, 배우5", movieVo.getCastNames());
        assertEquals("15", movieVo.getAgeRating());

        JsonNode tvRoot = json("{"
                + "\"genres\":[{\"name\":\"드라마\"}],"
                + "\"episode_run_time\":[55],\"number_of_episodes\":12,"
                + "\"created_by\":[{\"name\":\"크리에이터A\"},{\"name\":\"크리에이터A\"}],"
                + "\"credits\":{\"cast\":[{\"name\":\"배우A\"}]},"
                + "\"content_ratings\":{\"results\":["
                + "{\"iso_3166_1\":\"US\",\"rating\":\"TV-MA\"}]}}");
        TmdbVO tvVo = new TmdbVO();
        ReflectionTestUtils.invokeMethod(service, "fillTvDetail", tvVo, tvRoot);
        assertEquals(55, tvVo.getRuntime());
        assertEquals(12, tvVo.getEpisodeCount());
        assertEquals("크리에이터A", tvVo.getDirector());
        assertEquals("배우A", tvVo.getCastNames());
        assertEquals("18", tvVo.getAgeRating());
    }

    @Test
    void dateAndTextParsersShouldHandleNullInvalidDuplicatesAndLength() throws Exception {
        assertNull(ReflectionTestUtils.invokeMethod(service, "parseDate", (Object) null));
        assertNull(ReflectionTestUtils.invokeMethod(service, "parseDate", " "));
        assertNull(ReflectionTestUtils.invokeMethod(service, "parseDate", "2026/08/01"));
        assertEquals(
                LocalDate.of(2026, Month.AUGUST, 1),
                ReflectionTestUtils.invokeMethod(service, "parseDate", "2026-08-01"));

        assertNull(ReflectionTestUtils.invokeMethod(service, "parseGenreText", (Object) null));
        assertNull(ReflectionTestUtils.invokeMethod(service, "parseGenreText", json("{}")));
        assertNull(ReflectionTestUtils.invokeMethod(service, "parseGenreText", json("[]")));
        assertEquals(
                "액션, 드라마",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "parseGenreText",
                        json("[{\"name\":\"액션\"},{\"name\":\" \"},{\"name\":\"드라마\"}]")));

        assertNull(ReflectionTestUtils.invokeMethod(service, "parseDirector", json("{}")));
        assertNull(ReflectionTestUtils.invokeMethod(service, "parseTvCreator", json("{}")));
        assertNull(ReflectionTestUtils.invokeMethod(service, "parseCastNames", json("{}")));
        assertNull(ReflectionTestUtils.invokeMethod(service, "parseTvRuntime", json("[]")));

        assertEquals("abc", ReflectionTestUtils.invokeMethod(service, "limitLength", " abc ", 5));
        assertEquals("abc", ReflectionTestUtils.invokeMethod(service, "limitLength", "abcdef", 3));
        assertNull(ReflectionTestUtils.invokeMethod(service, "limitLength", null, 3));
        assertEquals("first", ReflectionTestUtils.invokeMethod(service, "firstNonBlank", "first", "second"));
        assertEquals("second", ReflectionTestUtils.invokeMethod(service, "firstNonBlank", " ", "second"));
    }

    @Test
    void movieAndTvAgeRatingsShouldPreferKoreaThenUseUsAndUnknown() throws Exception {
        JsonNode koreanMovie = json("{\"release_dates\":{\"results\":["
                + "{\"iso_3166_1\":\"US\",\"release_dates\":[{\"certification\":\"G\"}]},"
                + "{\"iso_3166_1\":\"KR\",\"release_dates\":[{\"certification\":\"청소년 관람불가\"}]}]}}");
        assertEquals("18", ReflectionTestUtils.invokeMethod(service, "extractMovieAgeRating", koreanMovie));

        JsonNode usMovie = json("{\"release_dates\":{\"results\":["
                + "{\"iso_3166_1\":\"JP\",\"release_dates\":[{\"certification\":\"PG12\"}]},"
                + "{\"iso_3166_1\":\"US\",\"release_dates\":[{\"certification\":\"PG-13\"}]}]}}");
        assertEquals("15", ReflectionTestUtils.invokeMethod(service, "extractMovieAgeRating", usMovie));
        assertEquals("UNKNOWN", ReflectionTestUtils.invokeMethod(
                service,
                "extractMovieAgeRating",
                json("{}")));

        JsonNode koreanTv = json("{\"content_ratings\":{\"results\":["
                + "{\"iso_3166_1\":\"KR\",\"rating\":\"12세\"},"
                + "{\"iso_3166_1\":\"US\",\"rating\":\"TV-MA\"}]}}");
        assertEquals("12", ReflectionTestUtils.invokeMethod(service, "extractTvAgeRating", koreanTv));
        assertEquals("UNKNOWN", ReflectionTestUtils.invokeMethod(
                service,
                "extractTvAgeRating",
                json("{}")));
    }

    @Test
    void ageRatingConvertersShouldCoverAllCountryAndCodeBranches() {
        assertNull(ReflectionTestUtils.invokeMethod(service, "convertAgeRating", "KR", null, false));
        assertNull(ReflectionTestUtils.invokeMethod(service, "convertAgeRating", "JP", "15", false));
        assertEquals("ALL", ReflectionTestUtils.invokeMethod(service, "convertAgeRating", "KR", "전체", false));
        assertEquals("ALL", ReflectionTestUtils.invokeMethod(service, "convertAgeRating", "KR", "7세", false));
        assertEquals("12", ReflectionTestUtils.invokeMethod(service, "convertAgeRating", "KR", "12세", false));
        assertEquals("15", ReflectionTestUtils.invokeMethod(service, "convertAgeRating", "KR", "15세", false));
        assertEquals("18", ReflectionTestUtils.invokeMethod(service, "convertAgeRating", "KR", "19세", false));
        assertEquals("UNKNOWN", ReflectionTestUtils.invokeMethod(service, "convertAgeRating", "KR", "미정", false));

        assertEquals("ALL", ReflectionTestUtils.invokeMethod(service, "convertAgeRating", "US", "TV-Y", true));
        assertEquals("12", ReflectionTestUtils.invokeMethod(service, "convertAgeRating", "US", "TV-PG", true));
        assertEquals("15", ReflectionTestUtils.invokeMethod(service, "convertAgeRating", "US", "TV-14", true));
        assertEquals("18", ReflectionTestUtils.invokeMethod(service, "convertAgeRating", "US", "TV-MA", true));
        assertEquals("UNKNOWN", ReflectionTestUtils.invokeMethod(service, "convertAgeRating", "US", "TV-X", true));

        assertEquals("ALL", ReflectionTestUtils.invokeMethod(service, "convertAgeRating", "US", "G", false));
        assertEquals("12", ReflectionTestUtils.invokeMethod(service, "convertAgeRating", "US", "PG", false));
        assertEquals("15", ReflectionTestUtils.invokeMethod(service, "convertAgeRating", "US", "PG-13", false));
        assertEquals("18", ReflectionTestUtils.invokeMethod(service, "convertAgeRating", "US", "NC-17", false));
        assertEquals("UNKNOWN", ReflectionTestUtils.invokeMethod(service, "convertAgeRating", "US", "UNRATED", false));
    }

    @Test
    void blockAndGenrePoliciesShouldHandleAdultKeywordsAndUnknownGenres() throws Exception {
        assertTrue(invokeBoolean("shouldExcludeContent", (Object) null));
        assertTrue(invokeBoolean("shouldExcludeContent", json("{\"adult\":true}")));
        assertTrue(invokeBoolean(
                "shouldExcludeContent",
                json("{\"title\":\"무삭제판 성인 영화\"}")));
        assertTrue(invokeBoolean(
                "shouldExcludeContent",
                json("{\"name\":\"Normal\",\"original_name\":\"Porn Movie\"}")));
        assertFalse(invokeBoolean(
                "shouldExcludeContent",
                json("{\"title\":\"정상 영화\",\"overview\":\"일반 줄거리\"}")));

        assertEquals("", ReflectionTestUtils.invokeMethod(service, "normalizeBlockedText", (Object) null));
        assertEquals("adultmovie19", ReflectionTestUtils.invokeMethod(
                service,
                "normalizeBlockedText",
                "Adult-Movie 19!"));
        assertEquals("액션, 드라마", ReflectionTestUtils.invokeMethod(
                service,
                "convertGenreIdsToText",
                json("[28,18,28,9999]"),
                "MOVIE"));
        assertEquals("액션·모험", ReflectionTestUtils.invokeMethod(
                service,
                "convertGenreIdsToText",
                json("[10759]"),
                "TV"));
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "convertGenreIdsToText",
                json("[9999]"),
                "MOVIE"));
    }

    @Test
    void nullableNumberHelpersShouldHandleMissingNullZeroAndValues() throws Exception {
        JsonNode values = json("{\"positive\":12,\"zero\":0,\"decimal\":8.5,\"nil\":null}");
        assertEquals(12, ReflectionTestUtils.<Integer>invokeMethod(
                service,
                "nullableInt",
                values.path("positive")));
        assertNull(ReflectionTestUtils.invokeMethod(service, "nullableInt", values.path("zero")));
        assertNull(ReflectionTestUtils.invokeMethod(service, "nullableInt", values.path("missing")));
        assertNull(ReflectionTestUtils.invokeMethod(service, "nullableInt", (Object) null));
        assertEquals(8.5, ReflectionTestUtils.<Double>invokeMethod(
                service,
                "nullableDouble",
                values.path("decimal")));
        assertNull(ReflectionTestUtils.invokeMethod(service, "nullableDouble", values.path("nil")));
        assertNull(ReflectionTestUtils.invokeMethod(service, "nullableDouble", (Object) null));
    }

    @Test
    void castAndCrewFilmographiesShouldFilterMergePrioritizeAndSort() throws Exception {
        JsonNode cast = json("["
                + "{\"id\":1,\"media_type\":\"movie\",\"title\":\"영화A\","
                + "\"original_title\":\"A\",\"release_date\":\"2026-08-01\","
                + "\"poster_path\":null,\"popularity\":10,\"character\":\"역할A\"},"
                + "{\"id\":1,\"media_type\":\"movie\",\"title\":\"영화A 재등록\","
                + "\"release_date\":\"2026-08-01\",\"poster_path\":\"/a.jpg\","
                + "\"popularity\":20,\"character\":\"역할B\"},"
                + "{\"id\":2,\"media_type\":\"tv\",\"name\":\"TV B\","
                + "\"first_air_date\":\"2025-01-01\",\"popularity\":30,\"character\":\"역할C\"},"
                + "{\"id\":0,\"media_type\":\"movie\",\"title\":\"무효\"},"
                + "{\"id\":3,\"media_type\":\"person\",\"title\":\"무효 유형\"},"
                + "{\"id\":4,\"media_type\":\"movie\",\"title\":\"성인영화\"}]");
        List<FilmographyVO> castList = ReflectionTestUtils.invokeMethod(
                service,
                "createCastFilmographyList",
                cast);
        assertEquals(2, castList.size());
        assertEquals(1L, castList.get(0).getTmdbId());
        assertEquals("/a.jpg", castList.get(0).getPosterPath());
        assertEquals("역할A, 역할B", castList.get(0).getParticipationName());
        assertEquals("CAST", castList.get(0).getParticipationCategory());

        JsonNode crew = json("["
                + "{\"id\":10,\"media_type\":\"movie\",\"title\":\"감독작\","
                + "\"release_date\":\"2024-01-01\",\"job\":\"Director\",\"department\":\"Directing\"},"
                + "{\"id\":11,\"media_type\":\"tv\",\"name\":\"제작작\","
                + "\"first_air_date\":\"2023-01-01\",\"job\":\"Producer\",\"department\":\"Production\"},"
                + "{\"id\":12,\"media_type\":\"movie\",\"title\":\"각본작\","
                + "\"job\":\"Writer\",\"department\":\"Writing\"}]");
        List<FilmographyVO> directors = ReflectionTestUtils.invokeMethod(
                service,
                "createCrewFilmographyList",
                crew,
                "DIRECTOR");
        List<FilmographyVO> production = ReflectionTestUtils.invokeMethod(
                service,
                "createCrewFilmographyList",
                crew,
                "PRODUCTION");
        assertEquals(1, directors.size());
        assertEquals("감독", directors.get(0).getParticipationName());
        assertEquals(2, production.size());
        assertEquals("PRODUCTION", production.get(0).getParticipationCategory());
    }

    @Test
    void participationNamesAndSortingComparatorsShouldCoverAllBranches() {
        assertFalse(invokeBoolean("isProductionParticipation", "Director", "Directing"));
        assertTrue(invokeBoolean("isProductionParticipation", "Creator", ""));
        assertTrue(invokeBoolean("isProductionParticipation", "", "Writing"));
        assertFalse(invokeBoolean("isProductionParticipation", "Actor", "Acting"));

        assertEquals("Department", ReflectionTestUtils.invokeMethod(
                service,
                "convertParticipationName",
                " ",
                "Department"));
        assertEquals("감독", participation("Director"));
        assertEquals("크리에이터", participation("Creator"));
        assertEquals("책임 프로듀서", participation("Executive Producer"));
        assertEquals("프로듀서", participation("Producer"));
        assertEquals("공동 프로듀서", participation("Co-Producer"));
        assertEquals("협력 프로듀서", participation("Associate Producer"));
        assertEquals("각본", participation("Writer"));
        assertEquals("각색", participation("Screenplay"));
        assertEquals("원안", participation("Story"));
        assertEquals("원작", participation("Novel"));
        assertEquals("음악", participation("Original Music Composer"));
        assertEquals("Editor", participation("Editor"));

        assertEquals("감독, 각본", ReflectionTestUtils.invokeMethod(
                service,
                "mergeParticipationNames",
                "감독, 각본",
                "각본"));
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "mergeParticipationNames",
                null,
                " "));

        FilmographyVO noPoster = filmography(1L, null, 10.0, "2026-01-01");
        FilmographyVO poster = filmography(2L, "/poster.jpg", 1.0, "2025-01-01");
        assertTrue((Integer) ReflectionTestUtils.invokeMethod(
                service,
                "compareFilmographyPriority",
                poster,
                noPoster) < 0);
        assertEquals(0, ReflectionTestUtils.<Integer>invokeMethod(
                service,
                "compareNullableDoubleDescending",
                null,
                null));
        assertTrue((Integer) ReflectionTestUtils.invokeMethod(
                service,
                "compareNullableDoubleDescending",
                null,
                1.0) > 0);
        assertTrue((Integer) ReflectionTestUtils.invokeMethod(
                service,
                "compareNullableDoubleDescending",
                1.0,
                null) < 0);

        Map<String, FilmographyVO> map = new LinkedHashMap<String, FilmographyVO>();
        FilmographyVO noDateHigh = filmography(3L, null, 50.0, null);
        FilmographyVO noDateLow = filmography(4L, null, 5.0, " ");
        map.put("no-date-low", noDateLow);
        map.put("poster", poster);
        map.put("no-date-high", noDateHigh);
        List<FilmographyVO> sorted = ReflectionTestUtils.invokeMethod(
                service,
                "sortFilmographyList",
                map);
        assertEquals(2L, sorted.get(0).getTmdbId());
        assertEquals(3L, sorted.get(1).getTmdbId());
    }

    private JsonNode json(String value) throws Exception {
        return jsonMapper.readTree(value);
    }

    private boolean invokeBoolean(String methodName, Object... arguments) {
        return Boolean.TRUE.equals(ReflectionTestUtils.invokeMethod(
                service,
                methodName,
                arguments));
    }

    private String participation(String job) {
        return ReflectionTestUtils.invokeMethod(
                service,
                "convertParticipationName",
                job,
                "");
    }

    private FilmographyVO filmography(
            Long id,
            String poster,
            Double popularity,
            String releaseDate) {
        FilmographyVO item = new FilmographyVO();
        item.setTmdbId(id);
        item.setPosterPath(poster);
        item.setPopularity(popularity);
        item.setReleaseDate(releaseDate);
        return item;
    }
}
