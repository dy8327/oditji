package com.project.oditji.favorite.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.favorite.dao.FavoriteDAO;
import com.project.oditji.favorite.vo.FavoriteVO;

@Service
public class FavoriteServiceImpl
        implements FavoriteService {

    private final FavoriteDAO favoriteDAO;

    public FavoriteServiceImpl(
            FavoriteDAO favoriteDAO) {

        this.favoriteDAO = favoriteDAO;
    }

    @Override
    @Transactional
    public boolean toggleFavorite(
            FavoriteVO favoriteVO) {

        validateFavorite(favoriteVO);

        int count =
                favoriteDAO.countFavorite(
                        favoriteVO);

        if (count > 0) {

            favoriteDAO.deleteFavorite(
                    favoriteVO);

            return false;
        }

        favoriteDAO.insertFavorite(
                favoriteVO);

        return true;
    }

    @Override
    public boolean isFavorite(
            FavoriteVO favoriteVO) {

        if (favoriteVO == null
                || favoriteVO.getMemberNo() == null
                || favoriteVO.getContentNo() == null) {

            return false;
        }

        return favoriteDAO.countFavorite(
                favoriteVO) > 0;
    }

    @Override
    public List<ContentVO> selectFavoriteList(
            Long memberNo) {

        if (memberNo == null) {
            return Collections.emptyList();
        }

        List<ContentVO> favoriteList =
                favoriteDAO.selectFavoriteList(
                        memberNo);

        return favoriteList == null
                ? Collections.emptyList()
                : favoriteList;
    }

    private void validateFavorite(
            FavoriteVO favoriteVO) {

        if (favoriteVO == null) {
            throw new IllegalArgumentException(
                    "찜 정보가 없습니다.");
        }

        if (favoriteVO.getMemberNo() == null) {
            throw new IllegalArgumentException(
                    "회원 정보가 없습니다.");
        }

        if (favoriteVO.getContentNo() == null) {
            throw new IllegalArgumentException(
                    "콘텐츠 정보가 없습니다.");
        }
    }
}