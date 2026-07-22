package com.project.oditji.wish.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.project.oditji.goods.vo.GoodsVO;
import com.project.oditji.wish.vo.WishVO;

@Mapper
public interface WishDAO {

    int countWish(WishVO wishVO);

    int insertWish(WishVO wishVO);

    int deleteWish(WishVO wishVO);

    List<GoodsVO> selectWishList(Long memberNo);

    /** 로그인 회원이 찜한 상품번호 목록 (목록/상세 화면의 찜 활성 표시용) */
    List<Integer> selectWishedProductNoList(Long memberNo);
}
