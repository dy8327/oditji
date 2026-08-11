package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.business.vo.EventManageVO;
import com.project.oditji.business.vo.GoodsManageVO;
import com.project.oditji.content.service.ContentService;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.search.service.SearchContentStore;
import com.project.oditji.tmdb.service.TmdbService;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/** 사업자 상품 삭제/이벤트 연장 성공 경로의 INFO 로그 guard false 분기를 보완합니다. */
class BusinessServiceImplResidualGuardCoverageTest {

    @TempDir
    Path tempDirectory;

    private BusinessDAO businessDAO;
    private NotificationService notificationService;
    private BusinessServiceImpl service;
    private Logger targetLogger;
    private Level originalLevel;

    @BeforeEach
    void setUp() {
        businessDAO = mock(BusinessDAO.class);
        notificationService = mock(NotificationService.class);
        service = new BusinessServiceImpl(
                businessDAO,
                mock(ContentService.class),
                mock(SearchContentStore.class),
                mock(TmdbService.class),
                notificationService,
                tempDirectory.resolve("product").toString(),
                tempDirectory.resolve("event").toString());
        targetLogger = (Logger) LoggerFactory.getLogger(BusinessServiceImpl.class);
        originalLevel = targetLogger.getLevel();
        targetLogger.setLevel(Level.OFF);
    }

    @AfterEach
    void tearDown() {
        targetLogger.setLevel(originalLevel);
    }

    @Test
    void productDeleteRequestShouldSucceedWhenInfoLoggingIsDisabled() {
        GoodsManageVO existing = new GoodsManageVO();
        existing.setProductName("삭제 요청 상품");
        existing.setStatus("APPROVED");
        when(businessDAO.selectProductForUpdate(10L, 20L)).thenReturn(existing);
        when(businessDAO.updateProductDeleteRequest(10L, 20L)).thenReturn(1);

        service.requestProductDelete(10L, 20L, "  삭제 사유  ");

        verify(notificationService).createForAdmins(
                "PRODUCT_DELETE_REQUEST",
                "상품 삭제 승인 요청",
                "삭제 요청 상품 상품의 삭제 요청이 접수되었습니다.",
                "/admin/product/list?tab=delete",
                "PRODUCT",
                10L);
    }

    @Test
    void eventExtensionShouldSucceedWhenInfoLoggingIsDisabledAndConnectedListIsNull() {
        LocalDate currentEndDate = LocalDate.of(2026, Month.AUGUST, 20);
        LocalDate extendedEndDate = LocalDate.of(2026, Month.SEPTEMBER, 10);
        EventManageVO existing = new EventManageVO();
        existing.setTitle("연장 이벤트");
        existing.setEndDate(currentEndDate);
        when(businessDAO.selectApprovedEventForBusiness(30L, 40L)).thenReturn(existing);
        when(businessDAO.extendApprovedEvent(30L, 40L, extendedEndDate)).thenReturn(1);

        service.extendApprovedEvent(30L, 40L, extendedEndDate, "  연장 사유  ");

        assertEquals(Long.valueOf(40L), existing.getBusinessNo());
        assertTrueConnectedProductsEmpty(existing);
        verify(notificationService).createForAdmins(
                "EVENT_REQUEST",
                "이벤트 연장 승인 요청",
                "연장 이벤트 이벤트의 연장 승인 요청이 접수되었습니다.",
                "/admin/event/list?tab=waiting",
                "EVENT",
                30L);
    }

    private void assertTrueConnectedProductsEmpty(EventManageVO existing) {
        org.junit.jupiter.api.Assertions.assertTrue(existing.getConnectedProducts().isEmpty());
    }
}
