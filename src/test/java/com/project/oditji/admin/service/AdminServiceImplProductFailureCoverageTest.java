package com.project.oditji.admin.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.project.oditji.admin.dao.AdminDAO;
import com.project.oditji.notification.service.NotificationService;

/**
 * AdminServiceImpl의 이벤트/상품 승인·반려 실패 라인을 집중 보완합니다.
 */
class AdminServiceImplProductFailureCoverageTest {

    @TempDir
    Path tempDirectory;

    private AdminDAO adminDAO;
    private AdminServiceImpl service;

    @BeforeEach
    void setUp() {
        adminDAO = mock(AdminDAO.class);

        service = new AdminServiceImpl(
                adminDAO,
                mock(NotificationService.class),
                tempDirectory.toString());
    }

    @Test
    void eventApprovalAndRejectionShouldFailWhenNoRowWasUpdated() {
        when(adminDAO.updateEventStatus(
                1L,
                "APPROVED"))
                .thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.approveEvent(1L));

        when(adminDAO.updateEventStatus(
                2L,
                "REJECTED"))
                .thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.rejectEvent(2L));
    }

    @Test
    void approveProductShouldRejectMissingProductAndOrderedDeleteRequest() {
        when(adminDAO.selectProductStatusByNo(10L))
                .thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.approveProduct(10L));

        when(adminDAO.selectProductStatusByNo(11L))
                .thenReturn("DELETE_REQUESTED");
        when(adminDAO.countOrderItemByProductNo(11L))
                .thenReturn(1);

        assertThrows(
                IllegalStateException.class,
                () -> service.approveProduct(11L));
    }

    @Test
    void deleteRequestedProductShouldFailWhenFinalDeleteCountIsNotOne() {
        when(adminDAO.selectProductStatusByNo(12L))
                .thenReturn("DELETE_REQUESTED");
        when(adminDAO.countOrderItemByProductNo(12L))
                .thenReturn(0);
        when(adminDAO.selectProductImagePathList(12L))
                .thenReturn(List.of());
        when(adminDAO.deleteProduct(12L))
                .thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.approveProduct(12L));
    }

    @Test
    void normalApprovalAndRejectionShouldFailWhenUpdateCountIsNotOne() {
        when(adminDAO.selectProductStatusByNo(13L))
                .thenReturn("WAITING");
        when(adminDAO.updateProductStatus(
                13L,
                "APPROVED"))
                .thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.approveProduct(13L));

        when(adminDAO.selectProductStatusByNo(14L))
                .thenReturn("WAITING");
        when(adminDAO.updateProductStatus(
                14L,
                "REJECTED"))
                .thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.rejectProduct(14L));
    }

    @Test
    void rejectProductShouldRejectMissingProduct() {
        when(adminDAO.selectProductStatusByNo(15L))
                .thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.rejectProduct(15L));
    }
}
