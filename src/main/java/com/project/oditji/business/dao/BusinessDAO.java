package com.project.oditji.business.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.project.oditji.business.vo.ActorSearchVO;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.business.vo.ContentSearchVO;
import com.project.oditji.business.vo.EventManageVO;
import com.project.oditji.business.vo.GoodsManageVO;

@Mapper
public interface BusinessDAO {

        /*
         * =========================================================
         * 로그인 회원과 연결된 사업자 조회
         * =========================================================
         */
        BusinessVO selectBusinessByMemberNo(@Param("memberNo") long memberNo);

        /*
        * =========================================================
        * 사업자등록번호 중복 확인
        * =========================================================
        */
        int countByBusinessNumber(@Param("businessNumber") String businessNumber);

        /*
        * =========================================================
        * 사업자 회원가입 정보 등록
        * =========================================================
        */
        int insertBusiness(BusinessVO businessVO);

        /*
         * =========================================================
         * 상품 등록
         * =========================================================
         */
        int insertProduct(GoodsManageVO goodsManageVO);

        /*
         * =========================================================
         * 상품 대표 이미지 등록
         * =========================================================
         */
        int insertProductImage(GoodsManageVO goodsManageVO);

        /*
         * =========================================================
         * 콘텐츠 검색 목록
         * =========================================================
         */
        List<ContentSearchVO> selectBusinessContentList(@Param("keyword") String keyword);

        /*
         * =========================================================
         * 콘텐츠 단건 조회
         *
         * 제거하면 안 됨.
         * BusinessServiceImpl에서 콘텐츠 존재 여부 확인에 사용한다.
         * =========================================================
         */
        ContentSearchVO selectContentByNo(@Param("contentNo") long contentNo);

        /*
         * =========================================================
         * 선택한 콘텐츠에 연결된 배우 목록
         * =========================================================
         */
        List<ActorSearchVO> selectActorListByContentNo(@Param("contentNo") long contentNo);

        /*
         * =========================================================
         * 선택한 콘텐츠와 배우의 연결 여부 확인
         * =========================================================
         */
        int countContentActor(@Param("contentNo") long contentNo, @Param("actorNo") long actorNo);

        /*
         * =========================================================
         * 사업자가 등록한 상품 목록
         * =========================================================
         */
        List<GoodsManageVO> selectProductListByBusinessNo(@Param("businessNo") long businessNo);

        /*
         * =========================================================
         * 상품 수정 화면용 상품 단건 조회
         *
         * PRODUCT_NO와 BUSINESS_NO를 함께 검사하여
         * 다른 사업자의 상품에 접근할 수 없도록 한다.
         * =========================================================
         */
        GoodsManageVO selectProductForUpdate(@Param("productNo") long productNo, @Param("businessNo") long businessNo);

        /*
         * =========================================================
         * 상품 기본 정보 수정
         * =========================================================
         */
        int updateProduct(GoodsManageVO goodsManageVO);

        /*
         * =========================================================
         * 기존 대표 이미지 수정
         * =========================================================
         */
        int updateProductMainImage(GoodsManageVO goodsManageVO);

        /*
         * =========================================================
         * 상품 삭제 요청
         *
         * 실제 상품 행은 삭제하지 않고
         * STATUS를 DELETE_REQUESTED로 변경한다.
         * =========================================================
         */
        int updateProductDeleteRequest(@Param("productNo") long productNo, @Param("businessNo") long businessNo);

        /*
         * =========================================================
         * 이벤트 연결 상품 소유 여부 확인
         *
         * 로그인한 사업자가 등록한 상품인지 확인한다.
         * =========================================================
         */
        int countProductByBusinessNo(@Param("productNo") long productNo, @Param("businessNo") long businessNo);

        /*
         * =========================================================
         * 이벤트 등록
         * =========================================================
         */
        int insertEvent(EventManageVO eventManageVO);

        /*
         * =========================================================
         * 이벤트와 상품 연결 등록
         * =========================================================
         */
        int insertEventProduct(EventManageVO eventManageVO);

        /*
         * =========================================================
         * 사업자 이벤트 목록 조회
         *
         * EVENT_PRODUCT -> PRODUCT 경로로 사업자 소유권을 확인한다.
         * =========================================================
         */
        List<EventManageVO> selectEventListByBusinessNo(@Param("businessNo") long businessNo, @Param("keyword") String keyword);

        /*
         * =========================================================
         * 승인된 이벤트 단건 조회
         *
         * EVENT_NO와 BUSINESS_NO를 함께 검사한다.
         * =========================================================
         */
        EventManageVO selectApprovedEventForBusiness(@Param("eventNo") long eventNo, @Param("businessNo") long businessNo);

        /*
         * =========================================================
         * 승인된 이벤트 기본 정보 수정
         * =========================================================
         */
        int updateApprovedEvent(EventManageVO eventManageVO);

        /*
         * =========================================================
         * 이벤트 연결 상품 수정
         * =========================================================
         */
        int updateEventProduct(EventManageVO eventManageVO);

        /*
         * =========================================================
         * 승인된 이벤트 종료일 연장
         * =========================================================
         */
        int extendApprovedEvent(@Param("eventNo") long eventNo, @Param("businessNo") long businessNo, @Param("extendEndDate") java.time.LocalDate extendEndDate);
}
