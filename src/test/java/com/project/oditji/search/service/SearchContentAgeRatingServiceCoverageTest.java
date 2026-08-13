package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** 연령등급 수동 보완, 재조회, 제한 등급 제거와 후보 파일 출력을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class SearchContentAgeRatingServiceCoverageTest {

    @TempDir
    Path tempDirectory;

    @Mock
    private TmdbApiClient apiClient;

    @Mock
    private SearchContentPolicyService contentPolicyService;

    private SearchContentAgeRatingService service;
    private Path manualPath;
    private Path missingPath;

    @BeforeEach
    void setUp() {
        service = new SearchContentAgeRatingService(
                apiClient,
                contentPolicyService,
                new SearchContentAgeRatingResolver());

        manualPath = tempDirectory.resolve("manual-age-rating-overrides.json");
        missingPath = tempDirectory.resolve("missing/missing-age-rating-candidates.json");

        ReflectionTestUtils.setField(service, "retryEnabled", true);
        ReflectionTestUtils.setField(service, "retryMaxAttempts", 2);
        ReflectionTestUtils.setField(service, "retryMaxPerRefresh", 100);
        ReflectionTestUtils.setField(service, "workerCount", 0);
        ReflectionTestUtils.setField(service, "manualOverrideEnabled", true);
        ReflectionTestUtils.setField(service, "manualOverridePath", manualPath.toString());
        ReflectionTestUtils.setField(service, "missingCandidatePath", missingPath.toString());

        lenient().when(contentPolicyService.shouldExcludeContent(any()))
                .thenAnswer(invocation -> {
                    CachedContentVO content = invocation.getArgument(0);
                    return content != null && "차단".equals(content.getTitle());
                });
        lenient().when(apiClient.getBaseUrl()).thenReturn("https://api.test/3");
    }

    @Test
    void reloadAndApplyShouldSupportSectionAndFlatManualFormats() throws IOException {
        String json = "{"
                + "\"MOVIE\":{\"100\":\"15\",\"bad\":\"12\",\"-1\":\"전체\"},"
                + "\"TV\":{\"200\":\"청소년 관람불가\",\"201\":\"unknown\"},"
                + "\"movie-300\":\"12세 이상 관람가\","
                + "\"TV-400\":\"7\","
                + "\"MOVIE-invalid\":\"15\","
                + "\"OTHER-500\":\"15\"} ";
        Files.writeString(manualPath, json, StandardCharsets.UTF_8);

        service.reload();

        CachedContentVO movie = content(100L, "MOVIE", "영화", "등급 정보 없음");
        CachedContentVO tv = content(200L, "TV", "TV", "등급 정보 없음");
        CachedContentVO flatMovie = content(300L, "movie", "평면 영화", "등급 정보 없음");
        CachedContentVO flatTv = content(400L, "tv", "평면 TV", "등급 정보 없음");
        CachedContentVO ignored = content(201L, "TV", "무효 등급", "등급 정보 없음");
        CachedContentVO blocked = content(100L, "MOVIE", "차단", "등급 정보 없음");

        service.applyManualOverrides(Arrays.asList(
                null,
                movie,
                tv,
                flatMovie,
                flatTv,
                ignored,
                blocked));

        assertEquals("15세 이상 관람가", movie.getAgeRating());
        assertEquals("청소년 관람불가", tv.getAgeRating());
        assertEquals("12세 이상 관람가", flatMovie.getAgeRating());
        assertEquals("7세 이상 관람가", flatTv.getAgeRating());
        assertEquals("등급 정보 없음", ignored.getAgeRating());
        assertEquals("등급 정보 없음", blocked.getAgeRating());

        service.applyManualOverrides(null);
        service.applyManualOverride(null);
    }

    @Test
    void reloadShouldHandleDisabledMissingAndMalformedFiles() throws IOException {
        ReflectionTestUtils.setField(service, "manualOverrideEnabled", false);
        service.reload();

        CachedContentVO content = content(1L, "MOVIE", "영화", "등급 정보 없음");
        service.applyManualOverride(content);
        assertEquals("등급 정보 없음", content.getAgeRating());

        ReflectionTestUtils.setField(service, "manualOverrideEnabled", true);
        ReflectionTestUtils.setField(service, "manualOverridePath", " ");
        service.reload();

        ReflectionTestUtils.setField(service, "manualOverridePath", manualPath.toString());
        service.reload();

        Files.writeString(manualPath, "{invalid", StandardCharsets.UTF_8);
        Runnable reload = service::reload;
        assertThrows(IllegalStateException.class, reload::run);
    }

    @Test
    void unknownRecheckShouldUpdateNormalRemoveRestrictedAndKeepFailures() {
        CachedContentVO normalMovie = content(1L, "MOVIE", "정상 영화", "등급 정보 없음");
        normalMovie.setPopularity(50.0);
        CachedContentVO restrictedTv = content(2L, "TV", "제한 TV", "등급 정보 없음");
        restrictedTv.setPopularity(100.0);
        CachedContentVO failed = content(3L, "MOVIE", "통신 실패", "등급 정보 없음");
        CachedContentVO unsupported = content(4L, "OTHER", "기타", "등급 정보 없음");
        CachedContentVO exhausted = content(5L, "MOVIE", "완료", "등급 정보 없음");
        exhausted.setAgeRatingRetryCount(2);
        CachedContentVO blocked = content(6L, "MOVIE", "차단", "등급 정보 없음");

        when(apiClient.get(anyString())).thenAnswer(invocation -> {
            String url = invocation.getArgument(0);
            if (url.contains("/movie/1/")) {
                return movieRatings("KR", "15");
            }
            if (url.contains("/tv/2/")) {
                return tvRatings("US", "TV-MA-S");
            }
            if (url.contains("/movie/3/")) {
                throw new IllegalStateException("network");
            }
            return new JSONObject();
        });

        List<CachedContentVO> contents = new ArrayList<CachedContentVO>(List.of(
                normalMovie,
                restrictedTv,
                failed,
                unsupported,
                exhausted,
                blocked));

        service.recheckUnknownAgeRatings(contents);

        assertTrue(contents.contains(normalMovie));
        assertFalse(contents.contains(restrictedTv));
        assertTrue(contents.contains(failed));
        assertEquals("15세 이상 관람가", normalMovie.getAgeRating());
        assertEquals(1, normalMovie.getAgeRatingRetryCount());
        assertNotNull(normalMovie.getAgeRatingLastCheckedAt());
        assertEquals(Boolean.TRUE, normalMovie.getAgeRatingRestrictionChecked());
        assertEquals(0, failed.getAgeRatingRetryCount() == null
                ? 0
                : failed.getAgeRatingRetryCount());

        ReflectionTestUtils.setField(service, "retryEnabled", false);
        service.recheckUnknownAgeRatings(contents);
        service.recheckUnknownAgeRatings(null);
        service.recheckUnknownAgeRatings(List.of());
    }

    @Test
    void restrictedRecheckShouldRemoveBlockedAndUpdateUnknownRating() throws JSONException {
        ReflectionTestUtils.setField(service, "workerCount", 99);
        ReflectionTestUtils.setField(service, "retryMaxPerRefresh", 1);

        CachedContentVO highPriority = content(10L, "MOVIE", "제한 영화", "청소년 관람불가");
        highPriority.setPopularity(100.0);
        CachedContentVO lowerPriority = content(11L, "TV", "정상 TV", "등급 정보 없음");
        lowerPriority.setPopularity(10.0);

        when(apiClient.get(anyString())).thenReturn(movieRatings("US", "NC-17"));

        List<CachedContentVO> limited = new ArrayList<CachedContentVO>(List.of(
                lowerPriority,
                highPriority));
        service.recheckRestrictedAgeRatings(limited);

        assertFalse(limited.contains(highPriority));
        assertTrue(limited.contains(lowerPriority));
        assertNotEquals(Boolean.TRUE, lowerPriority.getAgeRatingRestrictionChecked());

        ReflectionTestUtils.setField(service, "retryMaxPerRefresh", 100);
        when(apiClient.get(anyString())).thenAnswer(invocation -> {
            String url = invocation.getArgument(0);
            return url.contains("/tv/11/")
                    ? tvRatings("KR", "12")
                    : movieRatings("KR", "15");
        });

        service.recheckRestrictedAgeRatings(limited);
        assertEquals("12세 이상 관람가", lowerPriority.getAgeRating());
        assertEquals(Boolean.TRUE, lowerPriority.getAgeRatingRestrictionChecked());

        service.recheckRestrictedAgeRatings(null);
        service.recheckRestrictedAgeRatings(List.of());
    }

    @Test
    void missingCandidatesShouldWriteSortedMovieAndTvArrays() throws IOException, JSONException {
        CachedContentVO movie = content(20L, "MOVIE", "영화", "등급 정보 없음");
        movie.setAgeRatingRetryCount(2);
        movie.setPopularity(100.0);
        movie.setReleaseDate("2026-08-01");
        movie.setAgeRatingLastCheckedAt("2026-08-05T09:00:00");

        CachedContentVO tv = content(21L, "TV", "TV", "등급 정보 없음");
        tv.setAgeRatingRetryCount(3);
        tv.setPopularity(null);

        CachedContentVO insufficient = content(22L, "MOVIE", "재시도 부족", "등급 정보 없음");
        insufficient.setAgeRatingRetryCount(1);
        CachedContentVO rated = content(23L, "TV", "등급 있음", "15세 이상 관람가");
        rated.setAgeRatingRetryCount(3);
        CachedContentVO blocked = content(24L, "MOVIE", "차단", "등급 정보 없음");
        blocked.setAgeRatingRetryCount(3);

        service.writeMissingCandidates(Arrays.asList(
                null,
                tv,
                insufficient,
                rated,
                blocked,
                movie));

        JSONObject written = new JSONObject(Files.readString(
                missingPath,
                StandardCharsets.UTF_8));
        assertEquals(2, written.getInt("total"));
        assertEquals(1, written.getInt("movieCount"));
        assertEquals(1, written.getInt("tvCount"));
        assertEquals(20L, written.getJSONArray("MOVIE")
                .getJSONObject(0)
                .getLong("tmdbId"));
        assertEquals(21L, written.getJSONArray("TV")
                .getJSONObject(0)
                .getLong("tmdbId"));

        service.writeMissingCandidates(null);
        ReflectionTestUtils.setField(service, "missingCandidatePath", " ");
        service.writeMissingCandidates(List.of(movie));

        service.markLookupCompleted(movie);
        assertNotNull(movie.getAgeRatingLastCheckedAt());
        service.markLookupCompleted(null);
    }

    @Test
    void invalidMissingCandidatePathShouldWrapWriteFailure() throws IOException {
        Path blockingParent = tempDirectory.resolve("blocking-parent");
        Files.writeString(blockingParent, "file", StandardCharsets.UTF_8);
        Path invalidPath = blockingParent.resolve("missing.json");
        ReflectionTestUtils.setField(service, "missingCandidatePath", invalidPath.toString());

        CachedContentVO missing = content(30L, "MOVIE", "미작성", "등급 정보 없음");
        missing.setAgeRatingRetryCount(2);
        List<CachedContentVO> contents = List.of(missing);

        assertThrows(
                IllegalStateException.class,
                () -> service.writeMissingCandidates(contents));
    }

    private CachedContentVO content(
            Long tmdbId,
            String contentType,
            String title,
            String ageRating) {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(tmdbId);
        content.setContentType(contentType);
        content.setTitle(title);
        content.setAgeRating(ageRating);
        return content;
    }

    private JSONObject movieRatings(String countryCode, String certification) throws JSONException {
        JSONObject release = new JSONObject().put("certification", certification);
        JSONObject country = new JSONObject()
                .put("iso_3166_1", countryCode)
                .put("release_dates", new JSONArray().put(release));
        return new JSONObject().put("results", new JSONArray().put(country));
    }

    private JSONObject tvRatings(String countryCode, String rating) throws JSONException {
        JSONObject item = new JSONObject()
                .put("iso_3166_1", countryCode)
                .put("rating", rating);
        return new JSONObject().put("results", new JSONArray().put(item));
    }
}
