package com.project.oditji.favorite.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** 공개일까지 남은 일수 접근자의 잔여 라인을 검증합니다. */
class FavoriteReleaseTargetVOResidualCoverageTest {

    @Test
    void daysUntilReleaseGetterAndSetterShouldPreserveValueAndNull() {
        FavoriteReleaseTargetVO target = new FavoriteReleaseTargetVO();

        assertNull(target.getDaysUntilRelease());

        target.setDaysUntilRelease(1);
        assertEquals(Integer.valueOf(1), target.getDaysUntilRelease());

        target.setDaysUntilRelease(null);
        assertNull(target.getDaysUntilRelease());
    }
}
