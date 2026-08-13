package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** 국세청 API 호출 전에 사업자 입력값을 검증하는 분기를 확인합니다. */
class NtsBusinessServiceImplTest {

    private NtsBusinessServiceImpl ntsBusinessService;

    @BeforeEach
    void setUp() {
        ntsBusinessService = new NtsBusinessServiceImpl();
    }

    @Test
    void verifyBusinessShouldRequireBusinessNumber() {
        assertEquals(
                "사업자등록번호를 입력해주세요.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> ntsBusinessService.verifyBusiness(
                                " ",
                                "대표자",
                                "20260101"))
                        .getMessage());
    }

    @Test
    void verifyBusinessShouldRequireRepresentativeName() {
        assertEquals(
                "대표자명을 입력해주세요.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> ntsBusinessService.verifyBusiness(
                                "123-45-67890",
                                null,
                                "20260101"))
                        .getMessage());
    }

    @Test
    void verifyBusinessShouldRequireEightDigitOpenDate() {
        assertEquals(
                "개업일은 YYYYMMDD 형식으로 입력해주세요.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> ntsBusinessService.verifyBusiness(
                                "123-45-67890",
                                "대표자",
                                "2026-01-01"))
                        .getMessage());
    }
}
