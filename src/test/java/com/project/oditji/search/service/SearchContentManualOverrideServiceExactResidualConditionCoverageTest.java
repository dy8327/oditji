package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** 수동 OTT 보완 Map에 키는 있으나 플랫폼 Set이 비어 있는 잔여 분기를 보완합니다. */
class SearchContentManualOverrideServiceExactResidualConditionCoverageTest {

    private SearchContentManualOverrideService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentManualOverrideService(mock(TmdbProviderService.class));
    }

    @Test
    void emptyManualPlatformSetShouldLeaveExistingPlatformsUnchanged() {
        Map<String, Set<String>> overrides = new LinkedHashMap<String, Set<String>>();
        overrides.put("MOVIE:10", Set.of());
        overrideReference().set(overrides);

        CachedContentVO content = new CachedContentVO();
        content.setContentType("MOVIE");
        content.setTmdbId(10L);
        content.setPlatformKeys(List.of("wavve"));

        service.apply(content);

        assertEquals(List.of("wavve"), content.getPlatformKeys());
    }

    @Test
    void emptyOverrideMapShouldNotContainOtherwiseValidContent() {
        overrideReference().set(Map.of());

        CachedContentVO content = new CachedContentVO();
        content.setContentType("TV");
        content.setTmdbId(20L);

        assertFalse(service.contains(content));
    }

    @SuppressWarnings("unchecked")
    private AtomicReference<Map<String, Set<String>>> overrideReference() {
        return (AtomicReference<Map<String, Set<String>>>) ReflectionTestUtils.getField(
                service,
                "overrideMap");
    }
}
