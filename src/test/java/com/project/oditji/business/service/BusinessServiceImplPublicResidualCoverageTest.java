package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.business.vo.ContentSearchVO;
import com.project.oditji.business.vo.GoodsManageVO;
import com.project.oditji.content.service.ContentService;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.order.vo.OrderItemVO;
import com.project.oditji.search.service.SearchContentStore;
import com.project.oditji.tmdb.service.TmdbService;

/** 단순 위임형 사업자 서비스 메서드의 입력 검증과 null 반환 잔여 분기를 보완합니다. */
class BusinessServiceImplPublicResidualCoverageTest {

    @TempDir
    Path tempDirectory;

    private BusinessDAO businessDAO;
    private BusinessServiceImpl service;

    @BeforeEach
    void setUp() {
        businessDAO = mock(BusinessDAO.class);
        service = new BusinessServiceImpl(
                businessDAO,
                mock(ContentService.class),
                mock(SearchContentStore.class),
                mock(TmdbService.class),
                mock(NotificationService.class),
                tempDirectory.resolve("product").toString(),
                tempDirectory.resolve("event").toString());
    }

    @Test
    void businessLookupShouldValidateAndDelegate() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getBusinessByMemberNo(0L));

        BusinessVO business = new BusinessVO();
        when(businessDAO.selectBusinessByMemberNo(10L))
                .thenReturn(business);

        assertSame(
                business,
                service.getBusinessByMemberNo(10L));
        verify(businessDAO).selectBusinessByMemberNo(10L);
    }

    @Test
    void businessOrderItemsShouldValidateAndNormalizeNullList() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getBusinessOrderItemList(0L));

        when(businessDAO.selectBusinessOrderItemList(11L))
                .thenReturn(null);
        assertTrue(service.getBusinessOrderItemList(11L).isEmpty());

        List<OrderItemVO> items = List.of(new OrderItemVO());
        when(businessDAO.selectBusinessOrderItemList(12L))
                .thenReturn(items);
        assertSame(items, service.getBusinessOrderItemList(12L));
    }

    @Test
    void contentListShouldNormalizeNullBlankAndKeepKeyword() {
        when(businessDAO.selectBusinessContentList(null))
                .thenReturn(null);
        assertTrue(service.getContentList(null).isEmpty());
        assertTrue(service.getContentList("   ").isEmpty());

        List<ContentSearchVO> contents = List.of(new ContentSearchVO());
        when(businessDAO.selectBusinessContentList("drama"))
                .thenReturn(contents);

        assertSame(
                contents,
                service.getContentList("  drama  "));
    }

    @Test
    void contentLookupShouldRejectInvalidMissingAndReturnExistingContent() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getContentByNo(0L));

        when(businessDAO.selectContentByNo(20L))
                .thenReturn(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getContentByNo(20L));

        ContentSearchVO content = new ContentSearchVO();
        when(businessDAO.selectContentByNo(21L))
                .thenReturn(content);
        assertSame(content, service.getContentByNo(21L));
    }

    @Test
    void approvedProductsShouldValidateAndNormalizeNullList() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getApprovedProductListByBusinessNo(0L));

        when(businessDAO.selectApprovedProductListByBusinessNo(30L))
                .thenReturn(null);
        assertTrue(service.getApprovedProductListByBusinessNo(30L).isEmpty());

        List<GoodsManageVO> products = List.of(new GoodsManageVO());
        when(businessDAO.selectApprovedProductListByBusinessNo(31L))
                .thenReturn(products);
        assertEquals(
                products,
                service.getApprovedProductListByBusinessNo(31L));
    }
}
