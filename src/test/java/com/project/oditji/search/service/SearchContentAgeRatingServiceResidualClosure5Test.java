package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/**
 * SonarQube에 남은 연령등급 결과 guard, 기타 콘텐츠 유형,
 * 수동 보완 파일 IOException 분기를 보완합니다.
 */
class SearchContentAgeRatingServiceResidualClosure5Test {

    @TempDir
    Path tempDirectory;

    private SearchContentPolicyService policyService;
    private SearchContentAgeRatingService service;

    @BeforeEach
    void setUp() {
        policyService = mock(SearchContentPolicyService.class);

        service = new SearchContentAgeRatingService(
                mock(TmdbApiClient.class),
                policyService,
                mock(SearchContentAgeRatingResolver.class));
    }

    @Test
    void restrictedResultGuardShouldCoverNullAndUnsuccessfulResult()
            throws Exception {

        List<CachedContentVO> blocked =
                new ArrayList<CachedContentVO>();

        ReflectionTestUtils.invokeMethod(
                service,
                "applyRestrictedAgeRatingResult",
                (Object) null,
                blocked);

        CachedContentVO content = content(
                10L,
                "MOVIE",
                "등급 정보 없음",
                0);

        Object unsuccessfulResult =
                createAgeRatingResult(
                        content,
                        "등급 정보 없음",
                        false,
                        false);

        ReflectionTestUtils.invokeMethod(
                service,
                "applyRestrictedAgeRatingResult",
                unsuccessfulResult,
                blocked);

        assertTrue(blocked.isEmpty());
        assertEquals(
                "등급 정보 없음",
                content.getAgeRating());
    }

    @Test
    void missingCandidatesShouldCoverNonMovieAndNonTvType()
            throws Exception {

        Path output =
                tempDirectory.resolve("missing.json");

        ReflectionTestUtils.setField(
                service,
                "missingCandidatePath",
                output.toString());
        ReflectionTestUtils.setField(
                service,
                "retryMaxAttempts",
                2);

        CachedContentVO other = content(
                20L,
                "OTHER",
                "등급 정보 없음",
                2);
        other.setTitle("기타 유형");

        service.writeMissingCandidates(
                List.of(other));

        JSONObject root =
                new JSONObject(
                        Files.readString(output));

        assertEquals(1, root.getInt("total"));
        assertEquals(0, root.getInt("movieCount"));
        assertEquals(0, root.getInt("tvCount"));
    }

    @Test
    void reloadShouldWrapInvalidUtf8ReadAsIoFailure()
            throws Exception {

        Path invalidUtf8 =
                tempDirectory.resolve("manual-overrides.json");

        Files.write(
                invalidUtf8,
                new byte[] {
                        (byte) 0xC3,
                        (byte) 0x28
                });

        ReflectionTestUtils.setField(
                service,
                "manualOverrideEnabled",
                true);
        ReflectionTestUtils.setField(
                service,
                "manualOverridePath",
                invalidUtf8.toString());

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        service::reload);

        assertTrue(
                exception.getMessage().contains(
                        "수동 연령등급 보완 파일을 읽지 못했습니다"));
    }

    private Object createAgeRatingResult(
            CachedContentVO content,
            String ageRating,
            boolean success,
            boolean restricted)
            throws Exception {

        Class<?> resultClass =
                Class.forName(
                        SearchContentAgeRatingService.class.getName()
                                + "$AgeRatingResult");

        Constructor<?> constructor =
                resultClass.getDeclaredConstructor(
                        CachedContentVO.class,
                        String.class,
                        String.class,
                        boolean.class,
                        boolean.class);

        constructor.setAccessible(true);

        return constructor.newInstance(
                content,
                ageRating,
                "2026-08-18T17:00:00",
                success,
                restricted);
    }

    private CachedContentVO content(
            Long tmdbId,
            String contentType,
            String ageRating,
            int retryCount) {

        CachedContentVO content =
                new CachedContentVO();

        content.setTmdbId(tmdbId);
        content.setContentType(contentType);
        content.setAgeRating(ageRating);
        content.setAgeRatingRetryCount(retryCount);

        return content;
    }
}
