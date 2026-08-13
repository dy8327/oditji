package com.project.oditji.wish.service;

import java.util.List;
import java.util.Set;

import com.project.oditji.goods.vo.GoodsVO;
import com.project.oditji.wish.vo.WishVO;

public interface WishService {

    boolean toggleWish(WishVO wishVO);

    boolean isWished(WishVO wishVO);

    List<GoodsVO> selectWishList(Long memberNo);

    int getWishCount(Long memberNo);

    /** 로그인 회원이 찜한 상품번호 Set (목록/상세 화면에서 하트 활성 여부 표시용) */
    Set<Integer> getWishedProductNoSet(Long memberNo);
}
