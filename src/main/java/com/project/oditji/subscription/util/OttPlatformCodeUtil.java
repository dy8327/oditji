package com.project.oditji.subscription.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * OTT_DISCOUNT_INFO.PLATFORM_CODE(NETFLIX 등)와, 콘텐츠에 연결된
 * OTT_PLATFORM.PLATFORM_NAME(콘텐츠 쪽에서 실제로 쓰는 영문 플랫폼명,
 * 예: "Netflix", "wavve", "Disney Plus")을 서로 매칭하기 위한 고정 매핑입니다.
 *
 * 두 테이블이 플랫폼을 서로 다른 문자열로 표기하고 있어(OTT_DISCOUNT_INFO.PLATFORM_NAME은
 * "넷플릭스" 같은 한글 표시용 값, OTT_PLATFORM.PLATFORM_NAME은 콘텐츠 연결용 영문 값)
 * OTT 구독 조합 계산기에서 "이 콘텐츠를 볼 수 있는 플랫폼"과 "그 플랫폼의 요금"을
 * 이어주려면 이 매핑이 필요합니다.
 */
public final class OttPlatformCodeUtil {

    private OttPlatformCodeUtil() {
    }

    /** PLATFORM_CODE -> 콘텐츠 쪽 OTT_PLATFORM.PLATFORM_NAME(영문, DB 시드 데이터 기준) */
    private static final Map<String, String> CODE_TO_CONTENT_PLATFORM_NAME =
            new LinkedHashMap<String, String>();

    static {
        CODE_TO_CONTENT_PLATFORM_NAME.put("NETFLIX", "Netflix");
        CODE_TO_CONTENT_PLATFORM_NAME.put("TVING", "TVING");
        CODE_TO_CONTENT_PLATFORM_NAME.put("WAVVE", "wavve");
        CODE_TO_CONTENT_PLATFORM_NAME.put("DISNEY", "Disney Plus");
        CODE_TO_CONTENT_PLATFORM_NAME.put("WATCHA", "Watcha");
        CODE_TO_CONTENT_PLATFORM_NAME.put("COUPANG", "Coupangplay");
    }

    /**
     * 콘텐츠 platformName(영문, 대소문자·공백·'+' 표기 차이는 무시)으로
     * 일치하는 PLATFORM_CODE를 찾습니다. 매칭되는 코드가 없으면 null을 반환합니다.
     */
    public static String fromContentPlatformName(String contentPlatformName) {

        if (contentPlatformName == null) {

            return null;
        }

        String normalized = normalize(contentPlatformName);

        for (Map.Entry<String, String> entry
                : CODE_TO_CONTENT_PLATFORM_NAME.entrySet()) {

            if (normalize(entry.getValue()).equals(normalized)) {

                return entry.getKey();
            }
        }

        return null;
    }

    private static String normalize(String value) {

        return value.trim()
                .toLowerCase()
                .replace(" ", "")
                .replace("+", "plus");
    }
}
