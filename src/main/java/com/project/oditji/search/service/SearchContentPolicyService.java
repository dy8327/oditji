package com.project.oditji.search.service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.project.oditji.search.vo.CachedContentVO;

/**
 * ODITJI 검색 콘텐츠에 공통으로 적용할 노출 정책을 관리합니다.
 *
 * TMDB의 adult=false만으로 걸러지지 않는 명백한 포르노성 콘텐츠가
 * 검색 캐시에 포함되는 경우를 보완하기 위해 제목 기반 필터를 제공합니다.
 *
 * 일반적인 청소년 관람불가 영화와 단순히 등급 정보가 없는 정상 콘텐츠는
 * 유지하고, 명확한 성인물 표현이 제목에 포함된 경우만 제외합니다.
 */
@Service
public class SearchContentPolicyService {

    /**
     * 띄어쓰기와 특수문자를 제거한 뒤 검사해도 의미가 명확한 표현입니다.
     *
     * "섹스", "성인", "19", "XXX"처럼 정상 작품도 많이 포함할 수 있는
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
        if (content == null) {
            return false;
        }

        return shouldExcludeByTitle(
                content.getTitle(),
                content.getOriginalTitle()
        );
    }

    /**
     * TMDB Discover 단계처럼 VO 생성 전에도 사용할 수 있도록
     * 제목과 원제만 받아 동일한 정책을 적용합니다.
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

    private String normalizeCompact(String value) {
        return normalizeText(value)
                .replaceAll("[^\\p{L}\\p{N}]", "");
    }

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

    private String safeText(String value) {
        return value == null ? "" : value;
    }
}
