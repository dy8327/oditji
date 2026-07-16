package com.project.oditji.favorite.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.favorite.vo.FavoriteVO;

@Mapper
public interface FavoriteDAO {

    int countFavorite(FavoriteVO favoriteVO);

    int countFavoriteByTmdb(FavoriteVO favoriteVO);

    int insertFavorite(FavoriteVO favoriteVO);

    int deleteFavorite(FavoriteVO favoriteVO);

    List<ContentVO> selectFavoriteList(Long memberNo);
}