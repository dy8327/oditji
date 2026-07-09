package com.project.oditji.tmdb.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.project.oditji.tmdb.vo.ActorVO;
import com.project.oditji.tmdb.vo.DirectorVO;
import com.project.oditji.tmdb.vo.TmdbVO;

@Mapper
public interface TmdbDAO {

    int existsContent(
            @Param("tmdbId") Long tmdbId,
            @Param("contentType") String contentType);

    int insertContent(TmdbVO vo);

    List<TmdbVO> selectContentList();

    int updateContentDetail(TmdbVO vo);

    Integer findContentNo(
            @Param("tmdbId") Long tmdbId,
            @Param("contentType") String contentType);

    Integer findPlatformNo(
            @Param("platformName") String platformName);

    int existsContentPlatform(
            @Param("contentNo") Integer contentNo,
            @Param("platformNo") Integer platformNo);

    int insertContentPlatform(
            @Param("contentNo") Integer contentNo,
            @Param("platformNo") Integer platformNo);

    Integer findActorNoByTmdbId(
            @Param("tmdbActorId") Long tmdbActorId);

    int insertActor(ActorVO actor);

    int existsContentActor(
            @Param("contentNo") Integer contentNo,
            @Param("actorNo") Integer actorNo);

    int insertContentActor(
            @Param("contentNo") Integer contentNo,
            @Param("actorNo") Integer actorNo,
            @Param("characterName") String characterName,
            @Param("displayOrder") Integer displayOrder);

    Integer findDirectorNoByTmdbId(
            @Param("tmdbDirectorId") Long tmdbDirectorId);

    int insertDirector(DirectorVO director);

    int existsContentDirector(
            @Param("contentNo") Integer contentNo,
            @Param("directorNo") Integer directorNo);

    int insertContentDirector(
            @Param("contentNo") Integer contentNo,
            @Param("directorNo") Integer directorNo,
            @Param("directorType") String directorType,
            @Param("displayOrder") Integer displayOrder);
}