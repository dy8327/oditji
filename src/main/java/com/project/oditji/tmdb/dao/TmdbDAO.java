package com.project.oditji.tmdb.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.project.oditji.tmdb.vo.TmdbVO;

@Mapper
public interface TmdbDAO {

    int existsContent(
            @Param("tmdbId") Long tmdbId,
            @Param("contentType") String contentType
    );

    int insertContent(TmdbVO vo);

}