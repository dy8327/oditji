package com.project.oditji.search.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Service;

import com.project.oditji.search.vo.CachedContentVO;

/**
 * 검색용 콘텐츠를 서버 공용 메모리에 보관합니다.
 *
 * 전체 목록과 콘텐츠 키 Map을 함께 교체하여
 * 검색과 상세 진입 모두 빠르게 조회할 수 있도록 합니다.
 */
@Service
public class SearchContentStore {

    private final AtomicReference<List<CachedContentVO>> contentReference =
            new AtomicReference<List<CachedContentVO>>(List.of());

    private final AtomicReference<Map<String, CachedContentVO>> contentMapReference =
            new AtomicReference<Map<String, CachedContentVO>>(Map.of());

    private volatile Instant lastUpdatedAt;

    public List<CachedContentVO> getAll() {
        return contentReference.get();
    }

    /**
     * TMDB ID와 콘텐츠 유형으로 캐시 콘텐츠를 조회합니다.
     */
    public CachedContentVO findByTmdbIdAndContentType(
            Long tmdbId,
            String contentType) {

        if (tmdbId == null
                || contentType == null
                || contentType.isBlank()) {

            return null;
        }

        String key =
                contentType.trim().toUpperCase()
                        + "_"
                        + tmdbId;

        return contentMapReference.get().get(key);
    }

    /**
     * 전체 목록과 단건 조회 Map을 같은 시점에 새 데이터로 교체합니다.
     */
    public void replaceAll(
            List<CachedContentVO> newContentList) {

        List<CachedContentVO> mutableList =
                newContentList == null
                        ? new ArrayList<CachedContentVO>()
                        : new ArrayList<CachedContentVO>(
                                newContentList
                        );

        List<CachedContentVO> safeList =
                List.copyOf(mutableList);

        Map<String, CachedContentVO> mutableMap =
                new LinkedHashMap<String, CachedContentVO>();

        for (CachedContentVO content
                : safeList) {

            if (content == null) {
                continue;
            }

            String key =
                    content.createContentKey();

            if (!key.isEmpty()) {
                mutableMap.put(key, content);
            }
        }

        contentReference.set(safeList);
        contentMapReference.set(
                Map.copyOf(mutableMap)
        );

        lastUpdatedAt = Instant.now();
    }

    public int size() {
        return contentReference.get().size();
    }

    public boolean isEmpty() {
        return contentReference.get().isEmpty();
    }

    public Instant getLastUpdatedAt() {
        return lastUpdatedAt;
    }
}
