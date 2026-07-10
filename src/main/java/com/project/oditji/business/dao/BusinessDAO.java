package com.project.oditji.business.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.project.oditji.business.vo.ActorSearchVO;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.business.vo.ContentSearchVO;
import com.project.oditji.business.vo.GoodsManageVO;

@Mapper
public interface BusinessDAO {

    // 로그인 회원과 연결된 사업자 조회
    BusinessVO selectBusinessByMemberNo(long memberNo);

    // 상품 등록
    int insertProduct(GoodsManageVO goodsManageVO);

    // 상품 대표 이미지 등록
    int insertProductImage(GoodsManageVO goodsManageVO);

    // 콘텐츠 검색
    List<ContentSearchVO> selectContentList(@Param("keyword") String keyword);

    // 콘텐츠 존재 여부 검증
    ContentSearchVO selectContentByNo(long contentNo);

    // 배우 목록
    List<ActorSearchVO> selectActorList();

    // 배우 존재 여부 검증
    ActorSearchVO selectActorByNo(long actorNo);

    // 사업자 상품 목록
    List<GoodsManageVO> selectProductListByBusinessNo(long businessNo);
}