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

    // 로그인 회원과 연결된 사업자 정보
    BusinessVO selectBusinessByMemberNo(
            @Param("memberNo") long memberNo);

    // 상품 등록
    int insertProduct(GoodsManageVO goodsManageVO);

    // 상품 대표 이미지 등록
    int insertProductImage(GoodsManageVO goodsManageVO);

    // 콘텐츠 검색
    List<ContentSearchVO> selectContentList(
            @Param("keyword") String keyword);

    // 콘텐츠 단건 조회
    ContentSearchVO selectContentByNo(
            @Param("contentNo") long contentNo);

    // 전체 배우 목록
    List<ActorSearchVO> selectActorList();

    // 선택한 콘텐츠에 연결된 배우 목록
    List<ActorSearchVO> selectActorListByContentNo(
            @Param("contentNo") long contentNo);

    // 배우 단건 조회
    ActorSearchVO selectActorByNo(
            @Param("actorNo") long actorNo);

    // 콘텐츠와 배우 연결 관계 확인
    int countContentActor(
            @Param("contentNo") long contentNo,
            @Param("actorNo") long actorNo);

    // 사업자가 등록한 상품 목록
    List<GoodsManageVO> selectProductListByBusinessNo(
            @Param("businessNo") long businessNo);
}