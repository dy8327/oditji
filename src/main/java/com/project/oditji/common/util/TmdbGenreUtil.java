package com.project.oditji.common.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * TMDB 장르 ID와 ODITJI 한글 장르명을 공통으로 관리합니다.
 */
public final class TmdbGenreUtil {

    private static final Map<Integer, String> MOVIE_GENRES = createMovieGenres();
    private static final Map<Integer, String> TV_GENRES = createTvGenres();

    private TmdbGenreUtil() {
        // 인스턴스 생성 방지
    }

    public static Map<Integer, String> movieGenres() {
        return MOVIE_GENRES;
    }

    public static Map<Integer, String> tvGenres() {
        return TV_GENRES;
    }

    private static Map<Integer, String> createMovieGenres() {
        Map<Integer, String> genres = new LinkedHashMap<Integer, String>();
        genres.put(28, "액션");
        genres.put(12, "모험");
        genres.put(16, "애니메이션");
        genres.put(35, "코미디");
        genres.put(80, "범죄");
        genres.put(99, "다큐멘터리");
        genres.put(18, "드라마");
        genres.put(10751, "가족");
        genres.put(14, "판타지");
        genres.put(36, "역사");
        genres.put(27, "공포");
        genres.put(10402, "음악");
        genres.put(9648, "미스터리");
        genres.put(10749, "로맨스");
        genres.put(878, "SF");
        genres.put(10770, "TV 영화");
        genres.put(53, "스릴러");
        genres.put(10752, "전쟁");
        genres.put(37, "서부");
        return Collections.unmodifiableMap(genres);
    }

    private static Map<Integer, String> createTvGenres() {
        Map<Integer, String> genres = new LinkedHashMap<Integer, String>();
        genres.put(10759, "액션·모험");
        genres.put(16, "애니메이션");
        genres.put(35, "코미디");
        genres.put(80, "범죄");
        genres.put(99, "다큐멘터리");
        genres.put(18, "드라마");
        genres.put(10751, "가족");
        genres.put(10762, "키즈");
        genres.put(9648, "미스터리");
        genres.put(10763, "뉴스");
        genres.put(10764, "리얼리티");
        genres.put(10765, "SF·판타지");
        genres.put(10766, "연속극");
        genres.put(10767, "토크");
        genres.put(10768, "전쟁·정치");
        genres.put(37, "서부");
        return Collections.unmodifiableMap(genres);
    }
}
