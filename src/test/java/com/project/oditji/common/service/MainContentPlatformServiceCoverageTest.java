package com.project.oditji.common.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.vo.OttPlatformVO;

/**
 * 메인 콘텐츠 OTT 필터의 null/empty/선택 플랫폼 및 잘못된 플랫폼 행 조건을 검증합니다.
 */
class MainContentPlatformServiceCoverageTest {

    private MainContentPlatformService service;

    @BeforeEach
    void setUp() {
        service =
                new MainContentPlatformService();
    }

    @Test
    void attachShouldIgnoreNullAndEmptyContentLists() {
        service.attachPlatformLogos(
                null,
                List.of("Netflix"));

        service.attachPlatformLogos(
                List.of(),
                List.of("Netflix"));
    }

    @Test
    void nullOrEmptySelectionShouldKeepAllOriginalPlatforms() {
        OttPlatformVO netflix =
                platform("Netflix");
        OttPlatformVO tving =
                platform("TVING");

        SearchResultVO content =
                content(
                        Arrays.asList(
                                netflix,
                                tving));

        service.attachPlatformLogos(
                List.of(content),
                null);

        assertEquals(
                2,
                content.getPlatformList()
                        .size());

        service.attachPlatformLogos(
                List.of(content),
                Arrays.asList(
                        null,
                        " ",
                        "   "));

        assertEquals(
                2,
                content.getPlatformList()
                        .size());
    }

    @Test
    void selectedFilterShouldSkipNullPlatformAndNullPlatformName() {
        OttPlatformVO netflix =
                platform("Netflix");

        OttPlatformVO noName =
                new OttPlatformVO();

        SearchResultVO content =
                content(
                        Arrays.asList(
                                null,
                                noName,
                                netflix,
                                platform("TVING")));

        List<SearchResultVO> contents =
                new ArrayList<SearchResultVO>();
        contents.add(null);
        contents.add(content);

        service.attachPlatformLogos(
                contents,
                List.of(
                        "Netflix",
                        "넷플릭스"));

        assertEquals(
                1,
                content.getPlatformList()
                        .size());
        assertSame(
                netflix,
                content.getPlatformList()
                        .get(0));
    }

    @Test
    void privateFilterShouldReturnEmptyForNullAndEmptyPlatformLists() {
        @SuppressWarnings("unchecked")
        List<OttPlatformVO> nullResult =
                (List<OttPlatformVO>)
                        ReflectionTestUtils.invokeMethod(
                                service,
                                "filterSelectedPlatforms",
                                null,
                                java.util.Set.of(
                                        "netflix"));

        @SuppressWarnings("unchecked")
        List<OttPlatformVO> emptyResult =
                (List<OttPlatformVO>)
                        ReflectionTestUtils.invokeMethod(
                                service,
                                "filterSelectedPlatforms",
                                List.of(),
                                java.util.Set.of(
                                        "netflix"));

        assertTrue(nullResult.isEmpty());
        assertTrue(emptyResult.isEmpty());
    }

    private SearchResultVO content(
            List<OttPlatformVO> platforms) {

        SearchResultVO content =
                new SearchResultVO();
        content.setPlatformList(platforms);
        return content;
    }

    private OttPlatformVO platform(
            String name) {

        OttPlatformVO platform =
                new OttPlatformVO();
        platform.setPlatformName(name);
        return platform;
    }
}
