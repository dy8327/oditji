package com.project.oditji.search.service;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

/**
 * TMDB 제공처 목록을 조회하고 ODITJI 내부 플랫폼 키로 변환합니다.
 *
 * 터미널 출력은 하지 않으며, 누락된 제공처는 수동 OTT 보완 서비스가 처리합니다.
 */
@Service
public class TmdbProviderService {

    private final TmdbApiClient apiClient;

    public TmdbProviderService(TmdbApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public TmdbProviderRegistry loadRegistry() {
        Map<Integer, String> movieProviderMap = loadSupportedProviderMap("movie");
        Map<Integer, String> tvProviderMap = loadSupportedProviderMap("tv");

        TmdbProviderRegistry registry = new TmdbProviderRegistry(
                movieProviderMap,
                tvProviderMap
        );

        if (!registry.hasAnyProvider()) {
            throw new IllegalStateException(
                    "TMDB 제공처 목록에서 ODITJI 지원 OTT를 찾지 못했습니다."
            );
        }

        return registry;
    }

    private Map<Integer, String> loadSupportedProviderMap(String apiType) {
        String url = apiClient.getBaseUrl()
                + "/watch/providers/"
                + apiType
                + "?language="
                + apiClient.encode(apiClient.getLanguage())
                + "&watch_region="
                + apiClient.encode(apiClient.getRegion());

        JSONObject root = apiClient.get(url);
        JSONArray providers = root.optJSONArray("results");
        Map<Integer, String> result = new LinkedHashMap<Integer, String>();

        if (providers == null) {
            return result;
        }

        for (int index = 0; index < providers.length(); index++) {
            JSONObject provider = providers.optJSONObject(index);
            if (provider == null) {
                continue;
            }

            int providerId = provider.optInt("provider_id", 0);
            String platformKey = normalizePlatformName(
                    provider.optString("provider_name", "")
            );

            if (providerId > 0 && !platformKey.isEmpty()) {
                result.put(providerId, platformKey);
            }
        }

        return result;
    }

    public String normalizePlatformName(String name) {
        if (name == null) {
            return "";
        }

        String normalized = normalizeSearchText(name);

        if (normalized.contains("netflix") || normalized.contains("넷플릭스")) {
            return "netflix";
        }
        if (normalized.contains("tving") || normalized.contains("티빙")) {
            return "tving";
        }
        if (normalized.contains("wavve") || normalized.contains("웨이브")) {
            return "wavve";
        }
        if (normalized.contains("disney") || normalized.contains("디즈니")) {
            return "disney";
        }
        if (normalized.contains("watcha") || normalized.contains("왓챠")) {
            return "watcha";
        }
        if (normalized.contains("coupang") || normalized.contains("쿠팡")) {
            return "coupang";
        }

        return "";
    }

    private String normalizeSearchText(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]", "");
    }
}
