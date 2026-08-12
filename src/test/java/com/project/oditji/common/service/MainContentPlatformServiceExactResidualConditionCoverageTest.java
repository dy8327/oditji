package com.project.oditji.common.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.tmdb.vo.OttPlatformVO;

/** 선택 플랫폼 Set 자체가 null인 private helper 분기를 직접 보완합니다. */
class MainContentPlatformServiceExactResidualConditionCoverageTest {

    @Test
    void nullSelectedPlatformSetShouldKeepOriginalPlatformRows() {
        MainContentPlatformService service = new MainContentPlatformService();
        OttPlatformVO netflix = new OttPlatformVO();
        netflix.setPlatformName("Netflix");

        @SuppressWarnings("unchecked")
        List<OttPlatformVO> result = (List<OttPlatformVO>) ReflectionTestUtils.invokeMethod(
                service,
                "filterSelectedPlatforms",
                List.of(netflix),
                null);

        assertEquals(1, result.size());
        assertSame(netflix, result.get(0));
    }
}
