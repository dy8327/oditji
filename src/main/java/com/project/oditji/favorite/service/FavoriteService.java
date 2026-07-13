package com.project.oditji.favorite.service;

import java.util.List;

import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.favorite.vo.FavoriteVO;

public interface FavoriteService {

    // 찜 토글
    boolean toggleFavorite(FavoriteVO favoriteVO);

    // 내 찜 목록 조회
    List<ContentVO> selectFavoriteList(Long memberNo);

}