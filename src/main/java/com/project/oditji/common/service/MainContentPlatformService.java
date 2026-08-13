package com.project.oditji.common.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.project.oditji.common.util.PlatformNameNormalizer;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.vo.OttPlatformVO;

/**
 * JSONL에서 이미 연결된 OTT 목록을
 * 메인·추천 화면의 조건에 맞게 정리합니다.
 *
 * 이 서비스는 TMDB watch/providers API를 호출하지 않습니다.
 */
@Service
public class MainContentPlatformService {

    public void attachPlatformLogos(
            List<SearchResultVO> contentList,
            List<String> selectedPlatformNames) {

        if (contentList == null
                || contentList.isEmpty()) {

            return;
        }

        Set<String> selectedPlatformKeys =
                createSelectedPlatformKeys(
                        selectedPlatformNames
                );

        for (SearchResultVO content : contentList) {

            if (content == null) {
                continue;
            }

            List<OttPlatformVO> platformList =
                    content.getPlatformList();

            content.setPlatformList(
                    filterSelectedPlatforms(
                            platformList,
                            selectedPlatformKeys
                    )
            );
        }
    }

    private Set<String> createSelectedPlatformKeys(
            List<String> selectedPlatformNames) {

        Set<String> selectedKeys =
                new LinkedHashSet<String>();

        if (selectedPlatformNames == null) {
            return selectedKeys;
        }

        for (String platformName
                : selectedPlatformNames) {

            String key =
                    PlatformNameNormalizer.toKey(
                            platformName
                    );

            if (!key.isEmpty()) {
                selectedKeys.add(key);
            }
        }

        return selectedKeys;
    }

    private List<OttPlatformVO> filterSelectedPlatforms(
            List<OttPlatformVO> platformList,
            Set<String> selectedPlatformKeys) {

        List<OttPlatformVO> resultList =
                new ArrayList<OttPlatformVO>();

        if (platformList == null
                || platformList.isEmpty()) {

            return resultList;
        }

        if (selectedPlatformKeys == null
                || selectedPlatformKeys.isEmpty()) {

            resultList.addAll(
                    platformList
            );

            return resultList;
        }

        for (OttPlatformVO platform
                : platformList) {

            if (platform == null
                    || platform.getPlatformName() == null) {

                continue;
            }

            String key =
                    PlatformNameNormalizer.toKey(
                            platform.getPlatformName()
                    );

            if (selectedPlatformKeys.contains(key)) {
                resultList.add(platform);
            }
        }

        return resultList;
    }


}
