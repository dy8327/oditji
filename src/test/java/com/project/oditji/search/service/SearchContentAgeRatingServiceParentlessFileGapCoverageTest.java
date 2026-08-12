package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** 연령등급 JSON 저장 시 부모 경로가 없는 파일명의 분기를 보완합니다. */
class SearchContentAgeRatingServiceParentlessFileGapCoverageTest {

    @Test
    void jsonWriterShouldWriteFileWhenPathHasNoParentDirectory() throws Exception {
        SearchContentAgeRatingService service = new SearchContentAgeRatingService(
                mock(TmdbApiClient.class),
                mock(SearchContentPolicyService.class),
                mock(SearchContentAgeRatingResolver.class));

        String fileName = "age-rating-parentless-coverage-"
                + UUID.randomUUID()
                + ".json";
        Path path = Path.of(fileName);

        try {
            ReflectionTestUtils.invokeMethod(
                    service,
                    "writeJsonFile",
                    fileName,
                    new JSONObject().put("value", 1));

            assertTrue(Files.exists(path));
        } finally {
            Files.deleteIfExists(path);
        }
    }
}
