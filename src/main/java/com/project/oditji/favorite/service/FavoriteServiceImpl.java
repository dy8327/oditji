package com.project.oditji.favorite.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.favorite.dao.FavoriteDAO;
import com.project.oditji.favorite.vo.FavoriteVO;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteDAO favoriteDAO;

    public FavoriteServiceImpl(FavoriteDAO favoriteDAO) {
        this.favoriteDAO = favoriteDAO;
    }

    @Override
    public boolean toggleFavorite(FavoriteVO favoriteVO) {

        int count = favoriteDAO.countFavorite(favoriteVO);

        // 이미 찜한 경우 → 삭제
        if (count > 0) {
            favoriteDAO.deleteFavorite(favoriteVO);
            return false;
        }

        // 찜하지 않은 경우 → 추가
        favoriteDAO.insertFavorite(favoriteVO);
        return true;
    }

    @Override
    public List<ContentVO> selectFavoriteList(Long memberNo) {
        return favoriteDAO.selectFavoriteList(memberNo);
    }

}