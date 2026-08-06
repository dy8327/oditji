package com.project.oditji.common.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.project.oditji.member.vo.PlatformVO;

/**
 * 회원이 선택한 OTT 목록에서 중복 없이 플랫폼 이름만 추출합니다.
 */
public final class PlatformSelectionUtil {

    private PlatformSelectionUtil() {
        // 인스턴스 생성 방지
    }

    public static List<String> extractPlatformNames(
            List<PlatformVO> platformList) {

        if (platformList == null || platformList.isEmpty()) {
            return new ArrayList<String>();
        }

        Set<String> platformNameSet = new LinkedHashSet<String>();

        for (PlatformVO platform : platformList) {

            if (platform == null
                    || platform.getPlatformName() == null
                    || platform.getPlatformName().isBlank()) {
                continue;
            }

            platformNameSet.add(platform.getPlatformName());
        }

        return new ArrayList<String>(platformNameSet);
    }
}
