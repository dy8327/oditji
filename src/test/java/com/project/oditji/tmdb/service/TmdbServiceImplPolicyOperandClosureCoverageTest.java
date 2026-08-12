package com.project.oditji.tmdb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.tmdb.dao.TmdbDAO;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** 콘텐츠 제외 정책의 title/originalTitle 삼항 및 장르 타입 분기 잔여 조건을 보완합니다. */
class TmdbServiceImplPolicyOperandClosureCoverageTest {

    private TmdbServiceImpl service;
    private JsonMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder().build();
        service = new TmdbServiceImpl(mock(TmdbDAO.class), mapper);
    }

    @Test
    void contentPolicyShouldCoverNullTitleWithOriginalAndBothMissing() throws Exception {
        JsonNode originalOnly = mapper.readTree(
                "{\"title\":null,\"original_title\":\"Normal Movie\",\"overview\":\"safe\"}");
        JsonNode noTitles = mapper.readTree(
                "{\"title\":null,\"original_title\":null,\"name\":null,\"original_name\":null,\"overview\":\"safe\"}");

        assertFalse(invokeBoolean("shouldExcludeContent", originalOnly));
        assertFalse(invokeBoolean("shouldExcludeContent", noTitles));
    }

    @Test
    void genreNameResolverShouldCoverMovieAndTvTernaryBranches() {
        assertEquals("액션", invoke("resolveGenreName", 28, "MOVIE"));
        assertEquals("액션·모험", invoke("resolveGenreName", 10759, "TV"));
    }

    private boolean invokeBoolean(String methodName, Object... arguments) {
        Boolean result = invoke(methodName, arguments);
        return Boolean.TRUE.equals(result);
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(String methodName, Object... arguments) {
        return (T) ReflectionTestUtils.invokeMethod(
                service,
                methodName,
                arguments);
    }
}
