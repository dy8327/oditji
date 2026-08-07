package com.project.oditji.business.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.goods.vo.ProductOptionVO;

/** 상품 관리 VO의 null 방어와 세부 이미지 JSON escape 분기를 검증합니다. */
class GoodsManageVOBranchCoverageTest {

    @Test
    void nullCollectionsShouldBecomeEmptyCollections() {
        GoodsManageVO vo = new GoodsManageVO();

        vo.setOptionList(null);
        vo.setDetailImagePathList(null);

        assertNotNull(vo.getOptionList());
        assertTrue(vo.getOptionList().isEmpty());
        assertNotNull(vo.getDetailImagePathList());
        assertTrue(vo.getDetailImagePathList().isEmpty());
        assertEquals("[]", vo.getDetailImagesJson());
    }

    @Test
    void nonNullCollectionsShouldBePreserved() {
        GoodsManageVO vo = new GoodsManageVO();
        List<ProductOptionVO> options = new ArrayList<ProductOptionVO>();
        List<String> detailImages = new ArrayList<String>();

        vo.setOptionList(options);
        vo.setDetailImagePathList(detailImages);

        assertSame(options, vo.getOptionList());
        assertSame(detailImages, vo.getDetailImagePathList());
    }

    @Test
    void detailImagesJsonShouldEscapeEverySupportedCharacterAndNullItem() {
        GoodsManageVO vo = new GoodsManageVO();
        vo.setDetailImagePathList(List.of("a\\b\"c\r\nd.jpg", "normal.jpg"));

        assertEquals("[\"a\\\\b\\\"c\\r\\nd.jpg\",\"normal.jpg\"]", vo.getDetailImagesJson());

        List<String> withNull = new ArrayList<String>();
        withNull.add(null);
        withNull.add("second.jpg");
        vo.setDetailImagePathList(withNull);
        assertEquals("[\"\",\"second.jpg\"]", vo.getDetailImagesJson());
    }

    @Test
    void detailImagesJsonShouldAlsoHandleUnexpectedNullField() {
        GoodsManageVO vo = new GoodsManageVO();
        ReflectionTestUtils.setField(vo, "detailImagePathList", null);

        assertEquals("[]", vo.getDetailImagesJson());
    }

    @Test
    void toStringShouldContainCoreValues() {
        GoodsManageVO vo = new GoodsManageVO();
        vo.setProductNo(1L);
        vo.setBusinessNo(2L);
        vo.setContentNo(3L);
        vo.setProductName("상품");
        vo.setImagePath("/image.jpg");

        String text = vo.toString();

        assertTrue(text.contains("productNo=1"));
        assertTrue(text.contains("businessNo=2"));
        assertTrue(text.contains("productName='상품'"));
        assertFalse(text.isBlank());
    }
}
