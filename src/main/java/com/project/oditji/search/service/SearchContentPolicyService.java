package com.project.oditji.search.service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.project.oditji.search.vo.CachedContentVO;

/**
 * ODITJI 검색 콘텐츠에 공통으로 적용할 노출 정책을 관리합니다.
 *
 * TMDB의 adult=false만으로 걸러지지 않는 명백한 포르노성 콘텐츠가
 * 검색 캐시에 포함되는 경우를 보완합니다.
 *
 * 차단 정책은 두 단계로 구성합니다.
 * 1. 제목/원제에 명확한 성인물 표현이 포함된 경우 차단
 * 2. 제목만으로 안전하게 판정하기 어려운 작품은 TMDB ID + 콘텐츠 유형으로 차단
 *
 * 일반적인 청소년 관람불가 영화와 단순히 등급 정보가 없는 정상 콘텐츠는
 * 그대로 유지합니다.
 */
@Service
public class SearchContentPolicyService {

    /**
     * 직접 확인한 노출 제외 작품입니다.
     *
     * 제목은 번역/수정될 수 있으므로 TMDB ID와 콘텐츠 유형을 함께 사용합니다.
     * 키 형식: CONTENT_TYPE:TMDB_ID
     */
    private static final Set<String> BLOCKED_CONTENT_KEYS = Set.of(
            "TV:88090"
    );

    /**
     * 띄어쓰기와 특수문자를 제거한 뒤 검사해도 의미가 명확한 표현입니다.
     *
     * "섹스", "성인", "19"처럼 정상 작품에도 포함될 수 있는
     * 광범위한 단어는 의도적으로 사용하지 않습니다.
     */
    private static final List<String> COMPACT_BLOCKED_KEYWORDS = List.of(
            "성인영화",
            "성인비디오",
            "에로영화",
            "에로비디오",
            "포르노",
            "야동",
            "섹스무비",
            "무삭제판",
            "19금에로",
            "바람난형수님",
            "형수님참교육",
            "色情",
            "情色",
            "ポルノ",
            "アダルトビデオ",
            "成人映画"
    );

    /**
     * 영문 표현은 단어 경계를 확인하여 사람 이름 등의 일부 문자열이
     * 우연히 "porn"을 포함하는 경우를 차단하지 않도록 합니다.
     */
    private static final List<Pattern> WORD_BLOCKED_PATTERNS = List.of(
            Pattern.compile(
                    "(?<![\\p{L}\\p{N}])porn(?:o|ography|ographic)?(?![\\p{L}\\p{N}])",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
            ),
            Pattern.compile(
                    "(?<![\\p{L}\\p{N}])porn\\s*star(?![\\p{L}\\p{N}])",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
            ),
            Pattern.compile(
                    "(?<![\\p{L}\\p{N}])sex\\s*tape(?![\\p{L}\\p{N}])",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
            ),
            Pattern.compile(
                    "(?<![\\p{L}\\p{N}])sextape(?![\\p{L}\\p{N}])",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
            ),
            Pattern.compile(
                    "(?<![\\p{L}\\p{N}])adult\\s*(?:movie|film|video)(?![\\p{L}\\p{N}])",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
            )
    );

    /**
     * 캐시에 보관하려는 콘텐츠가 ODITJI 노출 제외 대상인지 확인합니다.
     */
    public boolean shouldExcludeContent(CachedContentVO content) {
        return content != null
                && shouldExcludeContent(
                        content.getTmdbId(),
                        content.getContentType(),
                        content.getTitle(),
                        content.getOriginalTitle()
                );
    }

    /**
     * TMDB Discover 단계처럼 CachedContentVO 생성 전에도 사용할 수 있도록
     * 식별값과 제목 정보를 받아 동일한 정책을 적용합니다.
     */
    public boolean shouldExcludeContent(
            Long tmdbId,
            String contentType,
            String title,
            String originalTitle) {

        return shouldExcludeByContentKey(tmdbId, contentType)
                || shouldExcludeByTitle(title, originalTitle);
    }

    /**
     * 직접 확인한 특정 작품을 TMDB ID + 콘텐츠 유형으로 차단합니다.
     */
    public boolean shouldExcludeByContentKey(
            Long tmdbId,
            String contentType) {

        String contentKey = createPolicyContentKey(tmdbId, contentType);
        return !contentKey.isEmpty()
                && BLOCKED_CONTENT_KEYS.contains(contentKey);
    }

    /**
     * 제목과 원제에 명확한 성인물 표현이 있는지 확인합니다.
     */
    public boolean shouldExcludeByTitle(
            String title,
            String originalTitle) {

        String combinedText = safeText(title) + " " + safeText(originalTitle);

        if (combinedText.isBlank()) {
            return false;
        }

        String compactText = normalizeCompact(combinedText);

        for (String keyword : COMPACT_BLOCKED_KEYWORDS) {
            if (compactText.contains(normalizeCompact(keyword))) {
                return true;
            }
        }

        String normalizedText = normalizeText(combinedText);

        for (Pattern pattern : WORD_BLOCKED_PATTERNS) {
            if (pattern.matcher(normalizedText).find()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 정책용 콘텐츠 식별 키를 생성합니다.
     */
    private String createPolicyContentKey(
            Long tmdbId,
            String contentType) {

        if (tmdbId == null
                || tmdbId <= 0
                || contentType == null
                || contentType.isBlank()) {

            return "";
        }

        return contentType.trim()
                .toUpperCase(Locale.ROOT)
                + ":"
                + tmdbId;
    }

    /**
     * 띄어쓰기와 특수문자를 제거한 비교용 문자열을 생성합니다.
     */
    private String normalizeCompact(String value) {
        return normalizeText(value)
                .replaceAll("[^\\p{L}\\p{N}]", "");
    }

    /**
     * 대소문자와 유니코드 표현 차이를 최소화하여 비교용 문자열을 생성합니다.
     */
    private String normalizeText(String value) {

        if (value == null) {
            return "";
        }

        return Normalizer.normalize(
                        value,
                        Normalizer.Form.NFKC
                )
                .toLowerCase(Locale.ROOT);
    }

    /**
     * null 문자열을 안전한 빈 문자열로 변환합니다.
     */
    private String safeText(String value) {
        return value == null ? "" : value;
    }
}