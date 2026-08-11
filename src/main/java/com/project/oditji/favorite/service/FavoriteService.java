package com.project.oditji.favorite.service;

import java.util.List;

import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.favorite.vo.FavoriteVO;

public interface FavoriteService {

    boolean toggleFavorite(FavoriteVO favoriteVO);

    boolean isFavorite(FavoriteVO favoriteVO);

    boolean isFavoriteByTmdb(FavoriteVO favoriteVO);

    List<ContentVO> selectFavoriteList(Long memberNo);

    /**
     * 내일 개봉·공개하는 콘텐츠를 찜한 회원 전원에게 알림을 생성합니다.
     *
     * 매일 한 번 스케줄러에서 호출합니다.
     *
     * @return 생성한 알림 건수
     */
    int notifyUpcomingReleases();
}