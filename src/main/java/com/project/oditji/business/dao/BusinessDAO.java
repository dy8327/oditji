package com.project.oditji.business.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.project.oditji.business.vo.ActorSearchVO;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.business.vo.ContentSearchVO;
import com.project.oditji.business.vo.EventManageVO;
import com.project.oditji.business.vo.EventProductVO;
import com.project.oditji.business.vo.GoodsManageVO;

@Mapper
public interface BusinessDAO {

        /*
         * =========================================================
         * 로그인 회원과 연결된 사업자 조회
         * =========================================================
         */
        BusinessVO selectBusinessByMemberNo(
                        @Param("memberNo") long memberNo);

        /*
         * =========================================================
         * 상품 등록
         * =========================================================
         */
        int insertProduct(
                        GoodsManageVO goodsManageVO);

        /*
         * =========================================================
         * 상품 대표 이미지 등록
         * =========================================================
         */
        int insertProductImage(
                        GoodsManageVO goodsManageVO);

        /*
         * =========================================================
         * 콘텐츠 검색 목록
         * =========================================================
         */
        List<ContentSearchVO> selectBusinessContentList(
                        @Param("keyword") String keyword);

        /*
         * =========================================================
         * 콘텐츠 단건 조회
         *
         * 제거하면 안 됨.
         * BusinessServiceImpl에서 콘텐츠 존재 여부 확인에 사용한다.
         * =========================================================
         */
        ContentSearchVO selectContentByNo(
                        @Param("contentNo") long contentNo);

        /*
         * =========================================================
         * 선택한 콘텐츠에 연결된 배우 목록
         * =========================================================
         */
        List<ActorSearchVO> selectActorListByContentNo(
                        @Param("contentNo") long contentNo);

        /*
         * =========================================================
         * 선택한 콘텐츠와 배우의 연결 여부 확인
         * =========================================================
         */
        int countContentActor(
                        @Param("contentNo") long contentNo,
                        @Param("actorNo") long actorNo);

        /*
         * =========================================================
         * 사업자가 등록한 상품 목록 (승인된 상품만)
         *
         * 이벤트 등록/수정 화면의 상품 검색 모달에서 사용한다.
         * 승인 대기(WAITING)/반려(REJECTED) 상품은 이벤트에 연결할 수
         * 없으므로 목록 자체에 포함하지 않는다.
         * =========================================================
         */
        List<GoodsManageVO> selectApprovedProductListByBusinessNo(
                        @Param("businessNo") long businessNo);

        /*
         * =========================================================
         * 사업자가 등록한 상품 목록
         * =========================================================
         */
        List<GoodsManageVO> selectProductListByBusinessNo(
                        @Param("businessNo") long businessNo);

        /*
         * =========================================================
         * 상품 수정 화면용 상품 단건 조회
         *
         * PRODUCT_NO와 BUSINESS_NO를 함께 검사하여
         * 다른 사업자의 상품에 접근할 수 없도록 한다.
         * =========================================================
         */
        GoodsManageVO selectProductForUpdate(
                        @Param("productNo") long productNo,
                        @Param("businessNo") long businessNo);

        /*
         * =========================================================
         * 상품 기본 정보 수정
         * =========================================================
         */
        int updateProduct(
                        GoodsManageVO goodsManageVO);

        /*
         * =========================================================
         * 기존 대표 이미지 수정
         * =========================================================
         */
        int updateProductMainImage(
                        GoodsManageVO goodsManageVO);

        /*
         * =========================================================
         * 상품 삭제 요청
         *
         * 실제 상품 행은 삭제하지 않고
         * STATUS를 DELETE_REQUESTED로 변경한다.
         * =========================================================
         */
        int updateProductDeleteRequest(
                        @Param("productNo") long productNo,
                        @Param("businessNo") long businessNo);

        /*
         * =========================================================
         * 이벤트 연결 상품 소유 여부 확인
         *
         * 로그인한 사업자가 등록한 상품인지 확인한다.
         * =========================================================
         */
        int countProductByBusinessNo(
                        @Param("productNo") long productNo,
                        @Param("businessNo") long businessNo);

        /*
         * =========================================================
         * 이벤트 등록
         * =========================================================
         */
        int insertEvent(
                        EventManageVO eventManageVO);

        /*
         * =========================================================
         * 이벤트와 상품 연결 등록
         *
         * 하나의 이벤트에 여러 상품을 연결할 수 있으므로
         * 상품 1건당 1회씩 호출한다. (Service 계층에서 반복 호출)
         * =========================================================
         */
        int insertEventProduct(
                        @Param("eventNo") long eventNo,
                        @Param("productNo") long productNo,
                        @Param("discountRate") int discountRate);

        /*
         * =========================================================
         * 이벤트에 연결된 상품 전체 삭제
         *
         * 이벤트 수정 시 기존 연결 상품을 모두 지우고
         * 새로 선택된 상품 목록을 다시 등록하는 방식으로 처리한다.
         * =========================================================
         */
        int deleteEventProductByEventNo(
                        @Param("eventNo") long eventNo);

        /*
         * =========================================================
         * 이벤트에 연결된 상품 목록 조회
         *
         * 이벤트 수정 화면 진입 시 기존에 연결되어 있던
         * 상품 목록을 그대로 화면에 다시 그려주기 위해 사용한다.
         * =========================================================
         */
        List<EventProductVO> selectEventProductListByEventNo(
                        @Param("eventNo") long eventNo);

        /*
         * =========================================================
         * 사업자 이벤트 목록 조회
         *
         * EVENT_PRODUCT -> PRODUCT 경로로 사업자 소유권을 확인한다.
         * =========================================================
         */
        List<EventManageVO> selectEventListByBusinessNo(
                        @Param("businessNo") long businessNo,
                        @Param("keyword") String keyword);

        /*
         * =========================================================
         * 승인된 이벤트 단건 조회
         *
         * EVENT_NO와 BUSINESS_NO를 함께 검사한다.
         * =========================================================
         */
        EventManageVO selectApprovedEventForBusiness(
                        @Param("eventNo") long eventNo,
                        @Param("businessNo") long businessNo);

        /*
         * =========================================================
         * 승인된 이벤트 기본 정보 수정
         * =========================================================
         */
        int updateApprovedEvent(
                        EventManageVO eventManageVO);

        /*
         * =========================================================
         * 승인된 이벤트 종료일 연장
         * =========================================================
         */
        int extendApprovedEvent(
                        @Param("eventNo") long eventNo,
                        @Param("businessNo") long businessNo,
                        @Param("extendEndDate") java.time.LocalDate extendEndDate);
}
