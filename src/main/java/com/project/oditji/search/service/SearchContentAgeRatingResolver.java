package com.project.oditji.search.service;

import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

/**
 * TMDB 영화·TV 연령등급 조회 결과를 ODITJI 내부 표기로 변환합니다.
 *
 * 상세 콘텐츠 보강과 누락 등급 재조회에서 동일한 판정 규칙을 사용하도록
 * 원본 제한 등급 검사와 국가별 등급 변환 로직을 한 곳에서 관리합니다.
 */
@Component
public class SearchContentAgeRatingResolver {

    private static final String MOVIE = "MOVIE";
    private static final String TV = "TV";

    private static final String JSON_RESULTS = "results";
    private static final String JSON_COUNTRY_CODE = "iso_3166_1";
    private static final String JSON_RELEASE_DATES = "release_dates";

    private static final String AGE_ALL = "전체 관람가";
    private static final String AGE_7 = "7세 이상 관람가";
    private static final String AGE_12 = "12세 이상 관람가";
    private static final String AGE_15 = "15세 이상 관람가";
    private static final String AGE_ADULT = "청소년 관람불가";
    private static final String AGE_UNKNOWN = "등급 정보 없음";

    /**
     * append_to_response가 포함된 상세 응답에서 서비스 제외 등급을 검사합니다.
     */
    public boolean hasRestrictedSourceAgeRating(
            String contentType,
            JSONObject detail) {

        if (detail == null
                || !hasText(contentType)) {
            return false;
        }

        if (MOVIE.equalsIgnoreCase(contentType)) {
            return hasRestrictedMovieRating(
                    detail.optJSONObject(JSON_RELEASE_DATES)
            );
        }

        if (TV.equalsIgnoreCase(contentType)) {
            return hasRestrictedTvRating(
                    detail.optJSONObject("content_ratings")
            );
        }

        return false;
    }

    /**
     * 영화 원본 등급 중 공용 저장소 제외 대상을 검사합니다.
     * JP R18+와 US NC-17이 대상입니다.
     */
    public boolean hasRestrictedMovieRating(
            JSONObject releaseDatesRoot) {

        if (releaseDatesRoot == null) {
            return false;
        }

        JSONArray countries =
                releaseDatesRoot.optJSONArray(JSON_RESULTS);

        return containsMovieCertification(
                countries,
                "JP",
                "R18+"
        )
                || containsMovieCertification(
                        countries,
                        "US",
                        "NC17"
                );
    }

    /**
     * 미국 TV 원본 등급 중 성적 상황 설명자(S)가 있는 TV-MA 조합을 검사합니다.
     */
    public boolean hasRestrictedTvRating(
            JSONObject contentRatingsRoot) {

        if (contentRatingsRoot == null) {
            return false;
        }

        JSONArray ratings =
                contentRatingsRoot.optJSONArray(JSON_RESULTS);

        return isRestrictedUsTvRating(
                findTvRating(
                        ratings,
                        "US"
                )
        );
    }

    private boolean containsMovieCertification(
            JSONArray countries,
            String countryCode,
            String restrictedCode) {

        if (countries == null
                || !hasText(countryCode)
                || !hasText(restrictedCode)) {
            return false;
        }

        JSONArray releaseDates =
                findMovieReleaseDates(
                        countries,
                        countryCode
                );

        return containsCertification(
                releaseDates,
                normalizeRatingCode(restrictedCode)
        );
    }

    private JSONArray findMovieReleaseDates(
            JSONArray countries,
            String countryCode) {

        for (int index = 0;
             index < countries.length();
             index++) {

            JSONObject country =
                    countries.optJSONObject(index);

            if (country != null
                    && countryCode.equalsIgnoreCase(
                            country.optString(
                                    JSON_COUNTRY_CODE,
                                    ""
                            )
                    )) {

                return country.optJSONArray(JSON_RELEASE_DATES);
            }
        }

        return null;
    }

    private boolean containsCertification(
            JSONArray releaseDates,
            String normalizedRestrictedCode) {

        if (releaseDates == null) {
            return false;
        }

        for (int releaseIndex = 0;
             releaseIndex < releaseDates.length();
             releaseIndex++) {

            JSONObject release =
                    releaseDates.optJSONObject(releaseIndex);

            if (release != null
                    && normalizedRestrictedCode.equals(
                            normalizeRatingCode(
                                    release.optString(
                                            "certification",
                                            ""
                                    )
                            )
                    )) {
                return true;
            }
        }

        return false;
    }

    private boolean isRestrictedUsTvRating(
            String rawRating) {

        String normalized =
                normalizeRatingCode(rawRating);

        return "TVMAS".equals(normalized)
                || "TVMALS".equals(normalized)
                || "TVMASV".equals(normalized)
                || "TVMALSV".equals(normalized);
    }

    private String normalizeRatingCode(
            String rawRating) {

        if (!hasText(rawRating)) {
            return "";
        }

        return rawRating.trim()
                .toUpperCase(Locale.ROOT)
                .replace(" ", "")
                .replace("-", "")
                .replace("_", "");
    }

    /**
     * 영화 release_dates 응답을 ODITJI 연령등급으로 변환합니다.
     */
    public String parseMovieAgeRating(
            JSONObject releaseDatesRoot) {

        if (releaseDatesRoot == null) {
            return AGE_UNKNOWN;
        }

        JSONArray countries =
                releaseDatesRoot.optJSONArray(JSON_RESULTS);

        String koreaRating =
                findMovieCertification(
                        countries,
                        "KR"
                );

        if (hasText(koreaRating)) {
            return normalizeKoreanAgeRating(koreaRating);
        }

        String japanRating =
                findMovieCertification(
                        countries,
                        "JP"
                );

        if (hasText(japanRating)) {
            String convertedJapanRating =
                    convertJapanMovieAgeRating(japanRating);

            if (!AGE_UNKNOWN.equals(convertedJapanRating)) {
                return convertedJapanRating;
            }
        }

        String usRating =
                findMovieCertification(
                        countries,
                        "US"
                );

        return hasText(usRating)
                ? convertUsMovieAgeRating(usRating)
                : AGE_UNKNOWN;
    }

    private String findMovieCertification(
            JSONArray countries,
            String countryCode) {

        if (countries == null) {
            return null;
        }

        for (int index = 0;
             index < countries.length();
             index++) {

            String certification =
                    findCountryMovieCertification(
                            countries.optJSONObject(index),
                            countryCode
                    );

            if (hasText(certification)) {
                return certification;
            }
        }

        return null;
    }

    private String findCountryMovieCertification(
            JSONObject country,
            String countryCode) {

        if (country == null
                || !countryCode.equalsIgnoreCase(
                        country.optString(
                                JSON_COUNTRY_CODE,
                                ""
                        )
                )) {
            return null;
        }

        JSONArray releaseDates =
                country.optJSONArray(JSON_RELEASE_DATES);

        if (releaseDates == null) {
            return null;
        }

        for (int releaseIndex = 0;
             releaseIndex < releaseDates.length();
             releaseIndex++) {

            JSONObject release =
                    releaseDates.optJSONObject(releaseIndex);

            if (release != null) {
                String certification =
                        release.optString(
                                "certification",
                                ""
                        ).trim();

                if (!certification.isEmpty()) {
                    return certification;
                }
            }
        }

        return null;
    }

    /**
     * TV content_ratings 응답을 ODITJI 연령등급으로 변환합니다.
     */
    public String parseTvAgeRating(
            JSONObject contentRatingsRoot) {

        if (contentRatingsRoot == null) {
            return AGE_UNKNOWN;
        }

        JSONArray ratings =
                contentRatingsRoot.optJSONArray(JSON_RESULTS);

        String koreaRating =
                findTvRating(
                        ratings,
                        "KR"
                );

        if (hasText(koreaRating)) {
            return normalizeKoreanAgeRating(koreaRating);
        }

        String usRating =
                findTvRating(
                        ratings,
                        "US"
                );

        return hasText(usRating)
                ? convertUsTvAgeRating(usRating)
                : AGE_UNKNOWN;
    }

    private String findTvRating(
            JSONArray ratings,
            String countryCode) {

        if (ratings == null) {
            return null;
        }

        for (int index = 0;
             index < ratings.length();
             index++) {

            JSONObject rating =
                    ratings.optJSONObject(index);

            if (rating == null
                    || !countryCode.equalsIgnoreCase(
                            rating.optString(
                                    JSON_COUNTRY_CODE,
                                    ""
                            )
                    )) {
                continue;
            }

            String value =
                    rating.optString(
                            "rating",
                            ""
                    ).trim();

            if (!value.isEmpty()) {
                return value;
            }
        }

        return null;
    }

    /**
     * 한국식 원문 등급 또는 숫자 표기를 프로젝트 표기로 정규화합니다.
     */
    public String normalizeKoreanAgeRating(
            String rawRating) {

        if (!hasText(rawRating)) {
            return AGE_UNKNOWN;
        }

        String normalized =
                rawRating.trim()
                        .toUpperCase(Locale.ROOT)
                        .replace(" ", "")
                        .replace("-", "")
                        .replace("_", "");

        if ("ALL".equals(normalized)
                || "전체".equals(normalized)
                || "전체관람가".equals(normalized)
                || "전체이용가".equals(normalized)
                || "0".equals(normalized)
                || "0+".equals(normalized)) {
            return AGE_ALL;
        }

        if ("7".equals(normalized)
                || "7+".equals(normalized)
                || normalized.contains("7세")) {
            return AGE_7;
        }

        if ("12".equals(normalized)
                || "12+".equals(normalized)
                || normalized.contains("12세")) {
            return AGE_12;
        }

        if ("15".equals(normalized)
                || "15+".equals(normalized)
                || normalized.contains("15세")) {
            return AGE_15;
        }

        if ("18".equals(normalized)
                || "18+".equals(normalized)
                || "19".equals(normalized)
                || "19+".equals(normalized)
                || normalized.contains("18세")
                || normalized.contains("19세")
                || normalized.contains("청소년관람불가")
                || normalized.contains("청불")
                || normalized.contains("제한상영가")) {
            return AGE_ADULT;
        }

        return AGE_UNKNOWN;
    }

    private String convertJapanMovieAgeRating(
            String rawRating) {

        if (!hasText(rawRating)) {
            return AGE_UNKNOWN;
        }

        String normalized =
                rawRating.trim()
                        .toUpperCase(Locale.ROOT)
                        .replace(" ", "")
                        .replace("_", "-")
                        .replace("-", "");

        return switch (normalized) {
            case "PG12" -> AGE_15;
            case "R15+", "R15" -> AGE_ADULT;
            default -> AGE_UNKNOWN;
        };
    }

    private String convertUsMovieAgeRating(
            String rawRating) {

        if (!hasText(rawRating)) {
            return AGE_UNKNOWN;
        }

        String normalized =
                rawRating.trim()
                        .toUpperCase(Locale.ROOT)
                        .replace(" ", "")
                        .replace("_", "-");

        return switch (normalized) {
            case "G" -> AGE_ALL;
            case "PG", "PG-13", "PG13" -> AGE_12;
            case "R" -> AGE_15;
            case "NC-17", "NC17" -> AGE_ADULT;
            default -> AGE_UNKNOWN;
        };
    }

    private String convertUsTvAgeRating(
            String rawRating) {

        if (!hasText(rawRating)) {
            return AGE_UNKNOWN;
        }

        String normalized =
                rawRating.trim()
                        .toUpperCase(Locale.ROOT)
                        .replace(" ", "")
                        .replace("_", "-");

        return switch (normalized) {
            case "TV-Y", "TVY", "TV-G", "TVG" -> AGE_ALL;
            case "TV-Y7", "TVY7" -> AGE_7;
            case "TV-PG", "TVPG" -> AGE_12;
            case "TV-14", "TV14" -> AGE_15;
            case "TV-MA", "TVMA", "TV-MA-S", "TV-MA-LS",
                    "TV-MA-SV", "TV-MA-LSV" -> AGE_ADULT;
            default -> AGE_UNKNOWN;
        };
    }

    public boolean isNormalizedAgeRating(
            String ageRating) {

        if (!hasText(ageRating)) {
            return false;
        }

        return AGE_ALL.equals(ageRating)
                || AGE_7.equals(ageRating)
                || AGE_12.equals(ageRating)
                || AGE_15.equals(ageRating)
                || AGE_ADULT.equals(ageRating)
                || AGE_UNKNOWN.equals(ageRating);
    }

    private boolean hasText(
            String value) {

        return value != null
                && !value.trim().isEmpty();
    }
}
