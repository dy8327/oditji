package com.project.oditji.event.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.event.dao.OttDiscountDAO;
import com.project.oditji.event.vo.OttDiscountVO;

/** OTT 할인 서비스의 정규화, 빈 목록 보정, CRUD 및 유효성 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class OttDiscountServiceImplCoverageTest {

    @Mock
    private OttDiscountDAO ottDiscountDAO;

    private OttDiscountServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OttDiscountServiceImpl(ottDiscountDAO);
    }

    @Test
    void listShouldNormalizeValuesCalculateOffsetAndKeepDaoList() {
        OttDiscountVO discount = new OttDiscountVO();
        when(ottDiscountDAO.selectDiscountList("NETFLIX", "CARD", 18, 9))
                .thenReturn(List.of(discount));

        List<OttDiscountVO> result = service.getDiscountList(" netflix ", " card ", 3, 9);

        assertEquals(List.of(discount), result);
        verify(ottDiscountDAO).selectDiscountList("NETFLIX", "CARD", 18, 9);
    }

    @Test
    void listShouldUseAllForNullOrBlankAndReturnEmptyForNullDaoResult() {
        when(ottDiscountDAO.selectDiscountList("ALL", "ALL", 0, 0))
                .thenReturn(null);

        List<OttDiscountVO> result = service.getDiscountList(null, "   ", 0, 0);

        assertEquals(List.of(), result);
        verify(ottDiscountDAO).selectDiscountList("ALL", "ALL", 0, 0);
    }

    @Test
    void countAndHeroShouldNormalizeFilters() {
        OttDiscountVO hero = new OttDiscountVO();
        when(ottDiscountDAO.selectDiscountListCount("ALL", "TV"))
                .thenReturn(12);
        when(ottDiscountDAO.selectHeroDiscount("DISNEY", "ALL"))
                .thenReturn(hero);

        assertEquals(12, service.getDiscountListCount(" ", " tv "));
        assertSame(hero, service.getHeroDiscount(" disney ", null));

        verify(ottDiscountDAO).selectDiscountListCount("ALL", "TV");
        verify(ottDiscountDAO).selectHeroDiscount("DISNEY", "ALL");
    }

    @Test
    void detailShouldRejectNullZeroAndNegativeAndLoadPositiveId() {
        assertNull(service.getDiscountDetail(null));
        assertNull(service.getDiscountDetail(0L));
        assertNull(service.getDiscountDetail(-1L));

        OttDiscountVO discount = new OttDiscountVO();
        when(ottDiscountDAO.selectDiscountById(4L)).thenReturn(discount);

        assertSame(discount, service.getDiscountDetail(4L));
        verify(ottDiscountDAO).selectDiscountById(4L);
    }

    @Test
    void adminListShouldCoverNullValidAndInvalidStatusNormalization() {
        OttDiscountVO discount = new OttDiscountVO();
        when(ottDiscountDAO.selectAdminDiscountList("ALL", "ALL", "ALL"))
                .thenReturn(null);
        when(ottDiscountDAO.selectAdminDiscountList("NETFLIX", "CARD", "Y"))
                .thenReturn(List.of(discount));
        when(ottDiscountDAO.selectAdminDiscountList("TVING", "EVENT", "N"))
                .thenReturn(List.of(discount));
        when(ottDiscountDAO.selectAdminDiscountList("WAVVE", "ETC", "ALL"))
                .thenReturn(List.of(discount));

        assertEquals(List.of(), service.getAdminDiscountList(null, " ", null));
        assertEquals(List.of(discount), service.getAdminDiscountList(" netflix ", " card ", " y "));
        assertEquals(List.of(discount), service.getAdminDiscountList("tving", "event", "N"));
        assertEquals(List.of(discount), service.getAdminDiscountList("wavve", "etc", "inactive"));
    }

    @Test
    void adminListBlankStatusShouldAlsoUseAll() {
        OttDiscountVO discount = new OttDiscountVO();
        when(ottDiscountDAO.selectAdminDiscountList("DISNEY", "CARD", "ALL"))
                .thenReturn(List.of(discount));

        assertEquals(
                List.of(discount),
                service.getAdminDiscountList("DISNEY", "CARD", "   "));
    }

    @Test
    void createUpdateAndDeleteShouldConvertAffectedRowsToBoolean() {
        OttDiscountVO createSuccess = new OttDiscountVO();
        OttDiscountVO createFailure = new OttDiscountVO();
        OttDiscountVO updateSuccess = new OttDiscountVO();
        OttDiscountVO updateFailure = new OttDiscountVO();

        when(ottDiscountDAO.insertDiscount(createSuccess)).thenReturn(1);
        when(ottDiscountDAO.insertDiscount(createFailure)).thenReturn(0);
        when(ottDiscountDAO.updateDiscount(updateSuccess)).thenReturn(2);
        when(ottDiscountDAO.updateDiscount(updateFailure)).thenReturn(0);
        when(ottDiscountDAO.deleteDiscount(10L)).thenReturn(1);
        when(ottDiscountDAO.deleteDiscount(11L)).thenReturn(0);

        assertTrue(service.createDiscount(createSuccess));
        assertFalse(service.createDiscount(createFailure));
        assertTrue(service.updateDiscount(updateSuccess));
        assertFalse(service.updateDiscount(updateFailure));
        assertTrue(service.deleteDiscount(10L));
        assertFalse(service.deleteDiscount(11L));
    }

    @Test
    void activateShouldRejectInvalidIdsAndCoverDaoResultBranches() {
        assertFalse(service.activateDiscount(null));
        assertFalse(service.activateDiscount(0L));
        assertFalse(service.activateDiscount(-1L));

        when(ottDiscountDAO.activateDiscount(20L)).thenReturn(1);
        when(ottDiscountDAO.activateDiscount(21L)).thenReturn(0);

        assertTrue(service.activateDiscount(20L));
        assertFalse(service.activateDiscount(21L));
    }

    @Test
    void expiredDiscountsShouldReturnDaoAffectedCount() {
        when(ottDiscountDAO.updateExpiredDiscounts()).thenReturn(6);

        assertEquals(6, service.deactivateExpiredDiscounts());
        verify(ottDiscountDAO).updateExpiredDiscounts();
    }
}
