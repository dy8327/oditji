package com.project.oditji.business.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.project.oditji.business.vo.ActorSearchVO;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.business.vo.ContentSearchVO;
import com.project.oditji.business.vo.EventManageVO;
import com.project.oditji.business.vo.GoodsManageVO;

public interface BusinessService {

        /*
         * =========================================================
         * 로그인 회원과 연결된 사업자 조회
         * =========================================================
         */
        BusinessVO getBusinessByMemberNo(long memberNo);

        /*
        * =========================================================
        * 사업자등록번호 사용 가능 여부 확인
        * =========================================================
        */
        boolean isBusinessNumberAvailable(
                String businessNumber);

        /*
         * =========================================================
         * 상품 등록
         * =========================================================
         */
        long registerProduct(GoodsManageVO goodsManageVO, MultipartFile productImage);

        /*
         * =========================================================
         * 콘텐츠 검색 목록 조회
         * =========================================================
         */
        List<ContentSearchVO> getContentList(String keyword);

        /*
         * =========================================================
         * 콘텐츠 단건 조회
         * =========================================================
         */
        ContentSearchVO getContentByNo(long contentNo);

        /*
         * =========================================================
         * 선택한 콘텐츠에 연결된 배우 목록 조회
         * =========================================================
         */
        List<ActorSearchVO> getActorListByContentNo(long contentNo);

        /*
         * =========================================================
         * 사업자가 등록한 상품 목록 조회
         * =========================================================
         */
        List<GoodsManageVO> getProductListByBusinessNo(long businessNo);

        /*
         * =========================================================
         * 상품 수정 화면용 상품 단건 조회
         *
         * 현재 로그인한 사업자의 상품인지 함께 확인한다.
         * =========================================================
         */
        GoodsManageVO getProductForUpdate(long productNo, long businessNo);

        /*
         * =========================================================
         * 상품 수정 요청
         *
         * 새 이미지가 전달되지 않으면 기존 이미지를 유지한다.
         * =========================================================
         */
        void updateProduct(GoodsManageVO goodsManageVO, MultipartFile productImage);

        /*
         * =========================================================
         * 상품 삭제 요청
         *
         * 상품을 즉시 삭제하지 않고
         * PRODUCT.STATUS를 DELETE_REQUESTED로 변경한다.
         * =========================================================
         */
        void requestProductDelete(long productNo, long businessNo, String reason);

        /*
         * =========================================================
         * 이벤트 등록
         *
         * EVENT 테이블에 이벤트를 저장하고,
         * 상품이 선택된 경우 EVENT_PRODUCT에도 연결 정보를 저장한다.
         * =========================================================
         */
        long registerEvent(EventManageVO eventManageVO, MultipartFile eventImage);

        /*
         * =========================================================
         * 사업자 이벤트 목록 조회
         * =========================================================
         */
        List<EventManageVO> getEventListByBusinessNo(long businessNo, String keyword);

        /*
         * =========================================================
         * 승인된 이벤트 단건 조회
         *
         * 현재 로그인한 사업자의 이벤트인지 함께 확인한다.
         * =========================================================
         */
        EventManageVO getApprovedEventForBusiness(long eventNo, long businessNo);

        /*
         * =========================================================
         * 승인된 이벤트 즉시 수정
         *
         * 별도 수정 요청 테이블이 없으므로
         * EVENT와 EVENT_PRODUCT를 즉시 변경한다.
         * =========================================================
         */
        void updateApprovedEvent(EventManageVO eventManageVO, MultipartFile eventImage);

        /*
         * =========================================================
         * 승인된 이벤트 즉시 연장
         *
         * 별도 연장 요청 테이블이 없으므로
         * EVENT.END_DATE를 즉시 변경한다.
         * =========================================================
         */
        void extendApprovedEvent(long eventNo, long businessNo, java.time.LocalDate extendEndDate, String extendReason);
}
