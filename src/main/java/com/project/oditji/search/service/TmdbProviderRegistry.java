package com.project.oditji.search.service;

import java.util.Map;
import java.util.Set;

/**
 * 영화와 TV에서 확인된 TMDB provider ID와 ODITJI 플랫폼 키 매핑을 보관합니다.
 */
public final class TmdbProviderRegistry {

    private static final String MOVIE = "MOVIE";

    private final Map<Integer, String> movieProviderMap;
    private final Map<Integer, String> tvProviderMap;

    public TmdbProviderRegistry(
            Map<Integer, String> movieProviderMap,
            Map<Integer, String> tvProviderMap) {

        this.movieProviderMap = movieProviderMap == null
                ? Map.of()
                : Map.copyOf(movieProviderMap);

        this.tvProviderMap = tvProviderMap == null
                ? Map.of()
                : Map.copyOf(tvProviderMap);
    }

    public Set<Integer> getMovieProviderIds() {
        return movieProviderMap.keySet();
    }

    public Set<Integer> getTvProviderIds() {
        return tvProviderMap.keySet();
    }

    public Map<Integer, String> getProviderMap(String contentType) {
        return MOVIE.equals(contentType)
                ? movieProviderMap
                : tvProviderMap;
    }

    public boolean hasAnyProvider() {
        return !movieProviderMap.isEmpty() || !tvProviderMap.isEmpty();
    }
}
