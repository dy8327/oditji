package com.project.oditji.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;

/**
 * DB, TMDB, JSONL에서 서로 다르게 전달되는 OTT 플랫폼명을
 * ODITJI 내부 키와 화면 표시명으로 통일합니다.
 */
public final class PlatformNameNormalizer {

    private static final String KEY_NETFLIX = "netflix";
    private static final String KEY_TVING = "tving";
    private static final String KEY_WAVVE = "wavve";
    private static final String KEY_DISNEY = "disney";
    private static final String KEY_WATCHA = "watcha";
    private static final String KEY_COUPANG = "coupang";

    private static final Set<String> SUPPORTED_KEYS =
            Set.of(
                    KEY_NETFLIX,
                    KEY_TVING,
                    KEY_WAVVE,
                    KEY_DISNEY,
                    KEY_WATCHA,
                    KEY_COUPANG
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

        if (normalized.contains(KEY_NETFLIX)
                || normalized.contains("넷플릭스")) {
            return KEY_NETFLIX;
        }

        if (normalized.contains(KEY_TVING)
                || normalized.contains("티빙")) {
            return KEY_TVING;
        }

        if (normalized.contains(KEY_WAVVE)
                || normalized.contains("웨이브")) {
            return KEY_WAVVE;
        }

        if (normalized.contains(KEY_DISNEY)
                || normalized.contains("디즈니")) {
            return KEY_DISNEY;
        }

        if (normalized.contains(KEY_WATCHA)
                || normalized.contains("왓챠")) {
            return KEY_WATCHA;
        }

        if (normalized.contains(KEY_COUPANG)
                || normalized.contains("쿠팡")) {
            return KEY_COUPANG;
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
            case KEY_NETFLIX -> "Netflix";
            case KEY_TVING -> "TVING";
            case KEY_WAVVE -> KEY_WAVVE;
            case KEY_DISNEY -> "Disney Plus";
            case KEY_WATCHA -> "Watcha";
            case KEY_COUPANG -> "Coupangplay";
            default -> null;
        };
    }
}
