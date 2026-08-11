package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

/** TMDB provider registry의 OR 단축평가 잔여 분기를 보완합니다. */
class TmdbProviderRegistryTailConditionCoverageTest {

    @Test
    void tvOnlyRegistryShouldUseSecondHasAnyProviderOperand() {
        TmdbProviderRegistry registry = new TmdbProviderRegistry(
                Map.of(),
                Map.of(97, "watcha"));

        assertTrue(registry.hasAnyProvider());
        assertTrue(registry.getMovieProviderIds().isEmpty());
        assertEquals(Map.of(97, "watcha"), registry.getProviderMap("TV"));
    }
}
