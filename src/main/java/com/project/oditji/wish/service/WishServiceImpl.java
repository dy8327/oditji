package com.project.oditji.wish.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.goods.vo.GoodsVO;
import com.project.oditji.wish.dao.WishDAO;
import com.project.oditji.wish.vo.WishVO;

@Service
public class WishServiceImpl
        implements WishService {

    private final WishDAO wishDAO;

    public WishServiceImpl(
            WishDAO wishDAO) {

        this.wishDAO = wishDAO;
    }

    @Override
    @Transactional
    public boolean toggleWish(
            WishVO wishVO) {

        validateWish(wishVO);

        int count =
                wishDAO.countWish(
                        wishVO);

        if (count > 0) {

            wishDAO.deleteWish(
                    wishVO);

            return false;
        }

        wishDAO.insertWish(
                wishVO);

        return true;
    }

    @Override
    public boolean isWished(
            WishVO wishVO) {

        if (wishVO == null
                || wishVO.getMemberNo() == null
                || wishVO.getProductNo() == null) {

            return false;
        }

        return wishDAO.countWish(
                wishVO) > 0;
    }

    @Override
        public int getWishCount(Long memberNo) {

        if (memberNo == null) {
                return 0;
        }

        return wishDAO.countWishByMemberNo(memberNo);
        }

    @Override
    public List<GoodsVO> selectWishList(
            Long memberNo) {

        if (memberNo == null) {
            return Collections.emptyList();
        }

        List<GoodsVO> wishList =
                wishDAO.selectWishList(
                        memberNo);

        return wishList == null
                ? Collections.emptyList()
                : wishList;
    }

    @Override
    public Set<Integer> getWishedProductNoSet(
            Long memberNo) {

        if (memberNo == null) {
            return Collections.emptySet();
        }

        List<Integer> productNoList =
                wishDAO.selectWishedProductNoList(
                        memberNo);

        if (productNoList == null
                || productNoList.isEmpty()) {

            return Collections.emptySet();
        }

        return new HashSet<Integer>(
                productNoList);
    }

    private void validateWish(
            WishVO wishVO) {

        if (wishVO == null) {
            throw new IllegalArgumentException(
                    "찜 정보가 없습니다.");
        }

        if (wishVO.getMemberNo() == null) {
            throw new IllegalArgumentException(
                    "회원 정보가 없습니다.");
        }

        if (wishVO.getProductNo() == null) {
            throw new IllegalArgumentException(
                    "상품 정보가 없습니다.");
        }
    }
}
