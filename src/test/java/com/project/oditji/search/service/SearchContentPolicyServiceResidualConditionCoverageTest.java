package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** 검색 노출 정책의 언어, 콘텐츠 키, 성인 키워드 단락 조건을 보완합니다. */
class SearchContentPolicyServiceResidualConditionCoverageTest {

    private SearchContentPolicyService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentPolicyService();
        ReflectionTestUtils.setField(service, "koreanEnglishTitleOnly", true);
    }

    @Test
    void cachedContentEntryShouldCoverNullAndBlockedContent() {
        assertFalse(service.shouldExcludeContent((CachedContentVO) null));

        CachedContentVO blocked = new CachedContentVO();
        blocked.setTmdbId(88090L);
        blocked.setContentType("tv");
        blocked.setTitle("일반 제목");
        assertTrue(service.shouldExcludeContent(blocked));
    }

    @Test
    void displayTitleLanguageShouldCoverDisabledNullBlankSupportedAndUnsupportedScripts() {
        ReflectionTestUtils.setField(service, "koreanEnglishTitleOnly", false);
        assertFalse(service.shouldExcludeByDisplayTitleLanguage("Русский"));

        ReflectionTestUtils.setField(service, "koreanEnglishTitleOnly", true);
        assertFalse(service.shouldExcludeByDisplayTitleLanguage(null));
        assertFalse(service.shouldExcludeByDisplayTitleLanguage("   "));
        assertFalse(service.shouldExcludeByDisplayTitleLanguage("한글 English 2026 !"));
        assertFalse(service.shouldExcludeByDisplayTitleLanguage("🎬 2026"));
        assertTrue(service.shouldExcludeByDisplayTitleLanguage("Русский"));
        assertTrue(service.shouldExcludeByDisplayTitleLanguage("日本語"));
    }

    @Test
    void contentKeyShouldCoverEveryValidationPositionAndCaseNormalization() {
        assertFalse(service.shouldExcludeByContentKey(null, "TV"));
        assertFalse(service.shouldExcludeByContentKey(0L, "TV"));
        assertFalse(service.shouldExcludeByContentKey(1L, null));
        assertFalse(service.shouldExcludeByContentKey(1L, "   "));
        assertFalse(service.shouldExcludeByContentKey(1L, "TV"));
        assertTrue(service.shouldExcludeByContentKey(88090L, " tv "));
    }

    @Test
    void titlePolicyShouldCoverBlankCompactAndWordBoundaryPatterns() {
        assertFalse(service.shouldExcludeByTitle(null, null));
        assertFalse(service.shouldExcludeByTitle("   ", "   "));
        assertTrue(service.shouldExcludeByTitle("성인 영화", null));
        assertTrue(service.shouldExcludeByTitle("A PORN STAR story", null));
        assertTrue(service.shouldExcludeByTitle("The sex tape", null));
        assertTrue(service.shouldExcludeByTitle("adult video collection", null));
        assertFalse(service.shouldExcludeByTitle("Scunthorpe documentary", null));
    }

    @Test
    void combinedPolicyShouldShortCircuitAcrossKeyTitleAndLanguageRules() {
        assertTrue(service.shouldExcludeContent(88090L, "TV", "정상 제목", null));
        assertTrue(service.shouldExcludeContent(1L, "MOVIE", "포르노", null));
        assertTrue(service.shouldExcludeContent(1L, "MOVIE", "ภาษาไทย", null));
        assertFalse(service.shouldExcludeContent(1L, "MOVIE", "Normal Movie", "원제"));
    }
}
