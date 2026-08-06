package com.project.oditji.common.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.project.oditji.tmdb.vo.OttPlatformVO;

/**
 * OTT 플랫폼 조회 결과를 화면에서 사용하는 플랫폼 키-로고 주소 Map으로 변환합니다.
 */
public final class OttPlatformUtil {

    private OttPlatformUtil() {
        // 인스턴스 생성 방지
    }

    public static Map<String, String> createLogoMap(
            List<OttPlatformVO> platformList) {

        Map<String, String> logoMap = new LinkedHashMap<String, String>();

        if (platformList == null) {
            return logoMap;
        }

        for (OttPlatformVO platform : platformList) {
            if (!hasUsableLogo(platform)) {
                continue;
            }

            String platformKey = PlatformNameNormalizer.toKey(
                    platform.getPlatformName());

            if (!platformKey.isEmpty()) {
                logoMap.put(platformKey, platform.getLogoImage());
            }
        }

        return logoMap;
    }

    private static boolean hasUsableLogo(OttPlatformVO platform) {
        return platform != null
                && platform.getPlatformName() != null
                && platform.getLogoImage() != null
                && !platform.getLogoImage().isBlank();
    }
}
