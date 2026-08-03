package com.project.oditji.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;

/**
 * DB, TMDB, JSONL에서 서로 다르게 전달되는 OTT 플랫폼명을
 * ODITJI 내부 키와 화면 표시명으로 통일합니다.
 */
public final class PlatformNameNormalizer {

    private static final Set<String> SUPPORTED_KEYS =
            Set.of(
                    "netflix",
                    "tving",
                    "wavve",
                    "disney",
                    "watcha",
                    "coupang"
            );

    private PlatformNameNormalizer() {
        /* 유틸리티 클래스이므로 인스턴스를 생성하지 않습니다. */
    }

    /**
     * 플랫폼명을 내부 소문자 키로 변환합니다.
     * 지원 목록에 없는 값은 검색 가능한 정규화 문자열을 반환합니다.
     */
    public static String toKey(
            String platformName) {

        if (platformName == null) {
            return "";
        }

        String normalized =
                Normalizer.normalize(
                        platformName,
                        Normalizer.Form.NFKC
                )
                        .trim()
                        .toLowerCase(Locale.ROOT)
                        .replaceAll(
                                "[^\\p{L}\\p{N}]",
                                ""
                        );

        if (normalized.contains("netflix")
                || normalized.contains("넷플릭스")) {
            return "netflix";
        }

        if (normalized.contains("tving")
                || normalized.contains("티빙")) {
            return "tving";
        }

        if (normalized.contains("wavve")
                || normalized.contains("웨이브")) {
            return "wavve";
        }

        if (normalized.contains("disney")
                || normalized.contains("디즈니")) {
            return "disney";
        }

        if (normalized.contains("watcha")
                || normalized.contains("왓챠")) {
            return "watcha";
        }

        if (normalized.contains("coupang")
                || normalized.contains("쿠팡")) {
            return "coupang";
        }

        return normalized;
    }

    /**
     * ODITJI에서 지원하는 6개 OTT만 내부 키로 반환합니다.
     */
    public static String toSupportedKey(
            String platformName) {

        String key = toKey(platformName);

        return SUPPORTED_KEYS.contains(key)
                ? key
                : "";
    }

    /**
     * 플랫폼명을 화면에서 사용하는 공식 표기로 변환합니다.
     */
    public static String toDisplayName(
            String platformName) {

        return switch (toSupportedKey(platformName)) {
            case "netflix" -> "Netflix";
            case "tving" -> "TVING";
            case "wavve" -> "wavve";
            case "disney" -> "Disney Plus";
            case "watcha" -> "Watcha";
            case "coupang" -> "Coupangplay";
            default -> null;
        };
    }
}
