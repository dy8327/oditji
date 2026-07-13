package com.project.oditji.business.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.project.oditji.business.vo.ActorSearchVO;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.business.vo.ContentSearchVO;
import com.project.oditji.business.vo.GoodsManageVO;

public interface BusinessService {

    // 로그인 회원과 연결된 사업자 조회
    BusinessVO getBusinessByMemberNo(long memberNo);

    // 상품 등록
    long registerProduct(
            GoodsManageVO goodsManageVO,
            MultipartFile productImage);

    // 콘텐츠 검색
    List<ContentSearchVO> getContentList(String keyword);

    // 콘텐츠 단건 조회
    ContentSearchVO getContentByNo(long contentNo);

    // 전체 배우 목록
    List<ActorSearchVO> getActorList();

    // 선택한 콘텐츠에 연결된 배우 목록
    List<ActorSearchVO> getActorListByContentNo(
            long contentNo);

    // 배우 단건 조회
    ActorSearchVO getActorByNo(long actorNo);

    // 사업자가 등록한 상품 목록
    List<GoodsManageVO> getProductListByBusinessNo(
            long businessNo);
}