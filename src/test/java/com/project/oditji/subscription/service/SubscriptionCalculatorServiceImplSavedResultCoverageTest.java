package com.project.oditji.subscription.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.event.dao.OttDiscountDAO;
import com.project.oditji.subscription.dao.SubscriptionDAO;
import com.project.oditji.subscription.vo.SubscriptionSavedResultVO;
import com.project.oditji.subscription.vo.SubscriptionShareVO;

/** 저장 결과 목록/삭제 기능에서 남은 null 및 JSON 방어 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class SubscriptionCalculatorServiceImplSavedResultCoverageTest {

    private static final Long MEMBER_NO = 31L;
    private static final String RESULT_ID = "SUBS_SAVED_1";

    @Mock
    private OttDiscountDAO ottDiscountDAO;

    @Mock
    private SubscriptionDAO subscriptionDAO;

    private SubscriptionCalculatorServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SubscriptionCalculatorServiceImpl(
                ottDiscountDAO,
                subscriptionDAO);
    }

    @Test
    void getSavedResultCountShouldReturnZeroForNullMemberWithoutQuerying() {
        assertEquals(0, service.getSavedResultCount(null));

        verifyNoInteractions(subscriptionDAO);
    }

    @Test
    void getSavedResultCountShouldReturnDaoListSize() {
        when(subscriptionDAO.selectResultsByMember(MEMBER_NO))
                .thenReturn(List.of(
                        share("SUBS_COUNT_1", "{}"),
                        share("SUBS_COUNT_2", "{}")));

        assertEquals(2, service.getSavedResultCount(MEMBER_NO));

        verify(subscriptionDAO).selectResultsByMember(MEMBER_NO);
    }

    @Test
    void getSavedResultsShouldReturnEmptyForNullMemberAndNullDaoList() {
        assertTrue(service.getSavedResultsByMember(null).isEmpty());

        when(subscriptionDAO.selectResultsByMember(MEMBER_NO))
                .thenReturn(null);

        assertTrue(service.getSavedResultsByMember(MEMBER_NO).isEmpty());
    }

    @Test
    void getSavedResultsShouldMapFieldsAndDefensivelyParseEveryJsonShape() {
        LocalDateTime createdAt = LocalDateTime.of(2026, Month.AUGUST, 13, 10, 0);

        SubscriptionShareVO valid = share(
                RESULT_ID,
                "{\"selectedPlatformList\":["
                        + "{\"platformName\":\"Netflix\"},"
                        + "{\"platformName\":null},"
                        + "{\"platformName\":\"   \"},"
                        + "{\"other\":\"ignored\"}]}" );
        valid.setCreatedAt(createdAt);
        valid.setTotalPrice(30000);
        valid.setDiscountPrice(7000);
        valid.setFinalPrice(23000);

        SubscriptionShareVO missingArray = share(
                "SUBS_NO_ARRAY",
                "{\"allPlatformMonthlyPrice\":10000}");
        SubscriptionShareVO nullJson = share("SUBS_NULL_JSON", null);
        SubscriptionShareVO blankJson = share("SUBS_BLANK_JSON", "   ");
        SubscriptionShareVO brokenJson = share("SUBS_BROKEN_JSON", "{broken-json");

        when(subscriptionDAO.selectResultsByMember(MEMBER_NO))
                .thenReturn(List.of(
                        valid,
                        missingArray,
                        nullJson,
                        blankJson,
                        brokenJson));

        List<SubscriptionSavedResultVO> resultList =
                service.getSavedResultsByMember(MEMBER_NO);

        assertEquals(5, resultList.size());

        SubscriptionSavedResultVO mapped = resultList.get(0);
        assertEquals(RESULT_ID, mapped.getResultId());
        assertEquals(createdAt, mapped.getCreatedAt());
        assertEquals(30000, mapped.getTotalPrice());
        assertEquals(7000, mapped.getDiscountPrice());
        assertEquals(23000, mapped.getFinalPrice());
        assertEquals(List.of("Netflix"), mapped.getPlatformNameList());

        assertTrue(resultList.get(1).getPlatformNameList().isEmpty());
        assertTrue(resultList.get(2).getPlatformNameList().isEmpty());
        assertTrue(resultList.get(3).getPlatformNameList().isEmpty());
        assertTrue(resultList.get(4).getPlatformNameList().isEmpty());
    }

    @Test
    void removeSavedResultShouldRejectInvalidArgumentsWithoutDeleting() {
        assertFalse(service.removeSavedResult(null, MEMBER_NO));
        assertFalse(service.removeSavedResult("   ", MEMBER_NO));
        assertFalse(service.removeSavedResult(RESULT_ID, null));

        verifyNoInteractions(subscriptionDAO);
    }

    @Test
    void removeSavedResultShouldReturnFalseWhenNothingWasDeleted() {
        when(subscriptionDAO.deleteResultByIdAndMember(RESULT_ID, MEMBER_NO))
                .thenReturn(0);

        assertFalse(service.removeSavedResult("  " + RESULT_ID + "  ", MEMBER_NO));

        verify(subscriptionDAO)
                .deleteResultByIdAndMember(RESULT_ID, MEMBER_NO);
    }

    @Test
    void removeSavedResultShouldReturnTrueWhenDaoDeletesRow() {
        when(subscriptionDAO.deleteResultByIdAndMember(RESULT_ID, MEMBER_NO))
                .thenReturn(1);

        assertTrue(service.removeSavedResult(RESULT_ID, MEMBER_NO));
    }

    @Test
    void savedResultPlatformListShouldRemainIndependentAcrossMappedRows() {
        SubscriptionShareVO first = share(
                "SUBS_FIRST",
                "{\"selectedPlatformList\":[{\"platformName\":\"TVING\"}]}");
        SubscriptionShareVO second = share(
                "SUBS_SECOND",
                "{\"selectedPlatformList\":[]}");

        when(subscriptionDAO.selectResultsByMember(MEMBER_NO))
                .thenReturn(List.of(first, second));

        List<SubscriptionSavedResultVO> resultList =
                service.getSavedResultsByMember(MEMBER_NO);

        List<String> firstPlatforms = resultList.get(0).getPlatformNameList();
        List<String> secondPlatforms = resultList.get(1).getPlatformNameList();

        assertEquals(List.of("TVING"), firstPlatforms);
        assertTrue(secondPlatforms.isEmpty());
        assertNotSame(firstPlatforms, secondPlatforms);
    }

    private SubscriptionShareVO share(
            String resultId,
            String selectedServicesJson) {

        SubscriptionShareVO shareVO = new SubscriptionShareVO();
        shareVO.setResultId(resultId);
        shareVO.setSelectedServicesJson(selectedServicesJson);
        return shareVO;
    }
}
