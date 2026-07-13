package com.project.oditji.favorite.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.favorite.vo.FavoriteVO;

@Mapper
public interface FavoriteDAO {

    // 찜 여부 확인
    int countFavorite(FavoriteVO favoriteVO);

    // 찜 추가
    int insertFavorite(FavoriteVO favoriteVO);

    // 찜 삭제
    int deleteFavorite(FavoriteVO favoriteVO);

    // 내 찜 목록 조회
    List<ContentVO> selectFavoriteList(Long memberNo);

}