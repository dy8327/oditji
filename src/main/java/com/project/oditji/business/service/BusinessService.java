package com.project.oditji.business.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.project.oditji.business.vo.ActorSearchVO;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.business.vo.ContentSearchVO;
import com.project.oditji.business.vo.GoodsManageVO;

public interface BusinessService {

        /*
         * =========================================================
         * 로그인 회원과 연결된 사업자 조회
         * =========================================================
         */
        BusinessVO getBusinessByMemberNo(
                        long memberNo);

        /*
         * =========================================================
         * 상품 등록
         * =========================================================
         */
        long registerProduct(
                        GoodsManageVO goodsManageVO,
                        MultipartFile productImage);

        /*
         * =========================================================
         * 콘텐츠 검색 목록 조회
         * =========================================================
         */
        List<ContentSearchVO> getContentList(
                        String keyword);

        /*
         * =========================================================
         * 콘텐츠 단건 조회
         * =========================================================
         */
        ContentSearchVO getContentByNo(
                        long contentNo);

        /*
         * =========================================================
         * 선택한 콘텐츠에 연결된 배우 목록 조회
         * =========================================================
         */
        List<ActorSearchVO> getActorListByContentNo(
                        long contentNo);

        /*
         * =========================================================
         * 사업자가 등록한 상품 목록 조회
         * =========================================================
         */
        List<GoodsManageVO> getProductListByBusinessNo(
                        long businessNo);

        /*
         * =========================================================
         * 상품 수정 화면용 상품 단건 조회
         *
         * 현재 로그인한 사업자의 상품인지 함께 확인한다.
         * =========================================================
         */
        GoodsManageVO getProductForUpdate(
                        long productNo,
                        long businessNo);

        /*
         * =========================================================
         * 상품 수정 요청
         *
         * 새 이미지가 전달되지 않으면 기존 이미지를 유지한다.
         * =========================================================
         */
        void updateProduct(
                        GoodsManageVO goodsManageVO,
                        MultipartFile productImage);
}