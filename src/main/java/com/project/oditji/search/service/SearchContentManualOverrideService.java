package com.project.oditji.search.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.project.oditji.search.vo.CachedContentVO;

/**
 * TMDB watch/providers에 누락된 OTT 정보를 수동 JSON으로 보완합니다.
 *
 * 수동 등록 콘텐츠는 Discover 결과에 없어도 TMDB ID와 콘텐츠 유형으로
 * 직접 후보를 생성하며, 상세 API 보강 후 지정된 플랫폼을 합칩니다.
 */
@Service
public class SearchContentManualOverrideService {

    private static final String MOVIE = "MOVIE";
    private static final String TV = "TV";

    private static final List<String> SUPPORTED_PLATFORM_KEYS = List.of(
            "netflix",
            "tving",
            "wavve",
            "disney",
            "watcha",
            "coupang"
    );

    @Value("${search.content-cache.manual-platform-override-enabled:false}")
    private boolean enabled;

    @Value("${search.content-cache.manual-platform-override-path:}")
    private String overridePath;

    private final TmdbProviderService providerService;

    private final AtomicReference<Map<String, Set<String>>> overrideMap =
            new AtomicReference<Map<String, Set<String>>>(Map.of());

    public SearchContentManualOverrideService(
            TmdbProviderService providerService) {
        this.providerService = providerService;
    }

    /**
     * 수집 시작 시 수동 JSON을 다시 읽어 최신 입력값을 반영합니다.
     */
    public void reload() {
        overrideMap.set(loadOverrides());
    }

    /**
     * 수동 JSON의 모든 TMDB ID를 상세 보강 대상 후보로 생성합니다.
     */
    public List<CachedContentVO> createCandidates() {
        List<CachedContentVO> result = new ArrayList<CachedContentVO>();

        for (String key : overrideMap.get().keySet()) {
            String[] parts = key.split(":", 2);
            if (parts.length != 2) {
                continue;
            }

            try {
                long tmdbId = Long.parseLong(parts[1]);
                CachedContentVO candidate = new CachedContentVO();
                candidate.setContentType(parts[0]);
                candidate.setTmdbId(tmdbId);
                result.add(candidate);
            } catch (NumberFormatException ignored) {
                /* 잘못된 ID 항목만 제외하고 나머지 수동 보완값은 유지합니다. */
            }
        }

        return result;
    }

    /**
     * TMDB 제공처 목록과 수동 플랫폼 목록을 중복 없이 합칩니다.
     */
    public void apply(CachedContentVO content) {
        if (content == null
                || content.getTmdbId() == null
                || !hasText(content.getContentType())) {
            return;
        }

        Set<String> manualPlatforms = overrideMap.get().get(
                createKey(content.getContentType(), content.getTmdbId())
        );

        if (manualPlatforms == null || manualPlatforms.isEmpty()) {
            return;
        }

        Set<String> merged = new LinkedHashSet<String>();
        if (content.getPlatformKeys() != null) {
            merged.addAll(content.getPlatformKeys());
        }
        merged.addAll(manualPlatforms);

        content.setPlatformKeys(new ArrayList<String>(merged));
    }

    /**
     * 수동 등록된 콘텐츠인지 확인합니다.
     */
    public boolean contains(CachedContentVO content) {
        return content != null
                && content.getTmdbId() != null
                && hasText(content.getContentType())
                && overrideMap.get().containsKey(
                        createKey(content.getContentType(), content.getTmdbId())
                );
    }

    private Map<String, Set<String>> loadOverrides() {
        if (!enabled) {
            return Map.of();
        }

        if (!hasText(overridePath)) {
            return Map.of();
        }

        Path path = Paths.get(overridePath);
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            return Map.of();
        }

        try {
            JSONObject root = new JSONObject(
                    Files.readString(path, StandardCharsets.UTF_8)
            );

            Map<String, Set<String>> result =
                    new LinkedHashMap<String, Set<String>>();

            readSection(root, MOVIE, result);
            readSection(root, TV, result);

            Map<String, Set<String>> immutable =
                    new LinkedHashMap<String, Set<String>>();

            for (Map.Entry<String, Set<String>> entry : result.entrySet()) {
                immutable.put(entry.getKey(), Set.copyOf(entry.getValue()));
            }

            return Map.copyOf(immutable);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "수동 OTT 보완 파일을 읽지 못했습니다: " + path.toAbsolutePath(),
                    e
            );
        } catch (JSONException e) {
            throw new IllegalStateException(
                    "수동 OTT 보완 JSON 형식이 올바르지 않습니다: " + path.toAbsolutePath(),
                    e
            );
        }
    }

    private void readSection(
            JSONObject root,
            String contentType,
            Map<String, Set<String>> target) {

        JSONObject section = root.optJSONObject(contentType);
        if (section == null) {
            return;
        }

        for (String rawPlatformKey : section.keySet()) {
            addManualPlatformOverrides(
                    section,
                    rawPlatformKey,
                    contentType,
                    target
            );
        }
    }


    private void addManualPlatformOverrides(
            JSONObject section,
            String rawPlatformKey,
            String contentType,
            Map<String, Set<String>> target) {

        String platformKey =
                providerService.normalizePlatformName(
                        rawPlatformKey
                );

        JSONArray tmdbIds =
                section.optJSONArray(
                        rawPlatformKey
                );

        if (!SUPPORTED_PLATFORM_KEYS.contains(platformKey)
                || tmdbIds == null) {

            return;
        }

        for (int index = 0; index < tmdbIds.length(); index++) {
            long tmdbId = tmdbIds.optLong(index, 0L);

            if (tmdbId > 0) {
                target.computeIfAbsent(
                        createKey(contentType, tmdbId),
                        ignored -> new LinkedHashSet<String>()
                ).add(platformKey);
            }
        }
    }

    private String createKey(String contentType, long tmdbId) {
        return contentType + ":" + tmdbId;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
