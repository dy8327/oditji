package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 기존 NTS 테스트에서 빠진 필수값 OR 조건의 반대 피연산자들을 보완합니다.
 */
class NtsBusinessServiceImplAdditionalRemainingCoverageTest {

    private NtsBusinessServiceImpl service;

    @BeforeEach
    void setUp() {
        service =
                new NtsBusinessServiceImpl();
    }

    @Test
    void blankBusinessNumberShouldUseBusinessNumberValidationMessage() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.verifyBusiness(
                                "   ",
                                "대표자",
                                "20260101"));

        assertEquals(
                "사업자등록번호를 입력해주세요.",
                exception.getMessage());
    }

    @Test
    void nullRepresentativeShouldUseRepresentativeValidationMessage() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.verifyBusiness(
                                "1234567890",
                                null,
                                "20260101"));

        assertEquals(
                "대표자명을 입력해주세요.",
                exception.getMessage());
    }

    @Test
    void nonNullInvalidOpenDateShouldUseDateValidationMessage() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.verifyBusiness(
                                "1234567890",
                                "대표자",
                                "2026-01-01"));

        assertEquals(
                "개업일은 YYYYMMDD 형식으로 입력해주세요.",
                exception.getMessage());
    }
}
