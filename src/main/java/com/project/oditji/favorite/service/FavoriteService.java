package com.project.oditji.favorite.service;

import java.util.List;

import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.favorite.vo.FavoriteVO;

public interface FavoriteService {

    boolean toggleFavorite(
            FavoriteVO favoriteVO);

    boolean isFavorite(
            FavoriteVO favoriteVO);

    List<ContentVO> selectFavoriteList(
            Long memberNo);
}