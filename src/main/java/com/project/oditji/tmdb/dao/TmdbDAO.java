package com.project.oditji.tmdb.dao;

import java.util.List;

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

    List<TmdbVO> selectContentList();

    int updateContentDetail(TmdbVO vo);

    Integer findPlatformNo(
            @Param("platformName") String platformName
    );

    Integer findContentNo(
            @Param("tmdbId") Long tmdbId,
            @Param("contentType") String contentType
    );

    int existsContentPlatform(
            @Param("contentNo") Integer contentNo,
            @Param("platformNo") Integer platformNo
    );

    int insertContentPlatform(
            @Param("contentNo") Integer contentNo,
            @Param("platformNo") Integer platformNo,
            @Param("watchUrl") String watchUrl
    );
}