package com.project.oditji.favorite.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.favorite.vo.FavoriteReleaseTargetVO;
import com.project.oditji.favorite.vo.FavoriteVO;

@Mapper
public interface FavoriteDAO {

    int countFavorite(FavoriteVO favoriteVO);

    int countFavoriteByTmdb(FavoriteVO favoriteVO);

    int insertFavorite(FavoriteVO favoriteVO);

    int deleteFavorite(FavoriteVO favoriteVO);

    List<ContentVO> selectFavoriteList(Long memberNo);

    int countFavoriteByMemberNo(Long memberNo);

    /**
     * 내일 개봉·공개하는 콘텐츠를 찜한 회원 목록을 조회합니다.
     *
     * 출시 알림 스케줄러가 매일 한 번 호출해
     * 각 대상 회원에게 알림을 생성하는 데 사용합니다.
     */
    List<FavoriteReleaseTargetVO> selectFavoriteReleaseTargetList();
}