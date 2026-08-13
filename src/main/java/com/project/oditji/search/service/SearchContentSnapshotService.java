package com.project.oditji.search.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.project.oditji.search.vo.CachedContentVO;

/**
 * 검색 콘텐츠 JSONL 스냅샷을 읽고 저장하는 서비스입니다.
 *
 * 저장 시 임시 파일에 먼저 전체 내용을 기록한 후
 * 원본 파일로 교체합니다.
 *
 * 따라서 중간 저장 도중 서버가 종료되어도
 * 기존 정상 스냅샷 파일이 손상되지 않습니다.
 */
@Service
public class SearchContentSnapshotService {

    private static final String JSON_TMDB_ID = "tmdbId";
    private static final String JSON_AGE_RATING_RETRY_COUNT = "ageRatingRetryCount";
    private static final String JSON_AGE_RATING_RESTRICTION_CHECKED = "ageRatingRestrictionChecked";
    private static final String JSON_RUNTIME = "runtime";
    private static final String JSON_EPISODE_COUNT = "episodeCount";
    private static final String JSON_TMDB_SCORE = "tmdbScore";
    private static final String JSON_POPULARITY = "popularity";

    @Value("${search.content-cache.snapshot-enabled:true}")
    private boolean snapshotEnabled;

    @Value("${search.content-cache.snapshot-path}")
    private String snapshotPath;

    private final SearchContentPolicyService contentPolicyService;

    public SearchContentSnapshotService(
            SearchContentPolicyService contentPolicyService) {

        this.contentPolicyService = contentPolicyService;
    }

    /**
     * 저장된 JSONL 스냅샷을 읽습니다.
     *
     * 손상된 한 줄이 있더라도 해당 줄만 제외하고
     * 나머지 정상 콘텐츠는 계속 복원합니다.
     *
     * @return 복원된 검색 콘텐츠 목록
     */
    public List<CachedContentVO> loadSnapshot() {

        List<CachedContentVO> result =
                new ArrayList<CachedContentVO>();

        if (!snapshotEnabled) {
            return result;
        }

        Path path =
                Path.of(snapshotPath);

        if (!Files.exists(path)) {
            return result;
        }

        try (BufferedReader reader =
                     Files.newBufferedReader(
                             path,
                             StandardCharsets.UTF_8
                     )) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                addSnapshotLine(
                        result,
                        line
                );
            }

        } catch (IOException e) {

            throw new IllegalStateException(
                    "검색 콘텐츠 스냅샷을 읽지 못했습니다.",
                    e
            );
        }

        return result;
    }

    private void addSnapshotLine(
            List<CachedContentVO> result,
            String line) {

        try {

            CachedContentVO content =
                    fromJson(
                            new JSONObject(line)
                    );

            if (contentPolicyService.shouldExcludeContent(content)) {
                return;
            }

            result.add(content);

        } catch (JSONException e) {

            /*
             * 특정 JSONL 한 줄이 손상되어도
             * 전체 스냅샷 복원을 중단하지 않습니다.
             */
        }
    }

    /**
     * 콘텐츠 목록을 JSONL로 원자적으로 저장합니다.
     *
     * @param contents 저장할 콘텐츠 목록
     */
    public void saveSnapshot(
            List<CachedContentVO> contents) {

        if (!snapshotEnabled) {
            return;
        }

        Path targetPath =
                Path.of(snapshotPath);

        Path parentPath =
                targetPath.getParent();

        try {

            if (parentPath != null) {

                Files.createDirectories(
                        parentPath
                );
            }

            /*
             * 기존 파일에 직접 쓰지 않고 임시 파일에 먼저 기록합니다.
             */
            Path temporaryPath =
                    Path.of(
                            snapshotPath + ".tmp"
                    );

            try (BufferedWriter writer =
                         Files.newBufferedWriter(
                                 temporaryPath,
                                 StandardCharsets.UTF_8
                         )) {

                if (contents != null) {

                    for (CachedContentVO content
                            : contents) {

                        if (content == null
                                || contentPolicyService.shouldExcludeContent(content)) {
                            continue;
                        }

                        writer.write(
                                toJson(content)
                                        .toString()
                        );

                        writer.newLine();
                    }
                }
            }

            /*
             * 임시 파일 기록이 모두 성공한 뒤에만
             * 기존 스냅샷을 교체합니다.
             */
            moveAtomically(
                    temporaryPath,
                    targetPath
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "검색 콘텐츠 스냅샷을 저장하지 못했습니다.",
                    e
            );
        }
    }

    /**
     * 운영체제가 원자 이동을 지원하면 ATOMIC_MOVE를 사용하고,
     * 지원하지 않으면 일반 교체 이동으로 대체합니다.
     */
    private void moveAtomically(
            Path source,
            Path target) throws IOException {

        try {

            Files.move(
                    source,
                    target,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );

        } catch (AtomicMoveNotSupportedException e) {

            Files.move(
                    source,
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    /**
     * CachedContentVO를 JSONL 한 줄용 JSONObject로 변환합니다.
     */
    private JSONObject toJson(
            CachedContentVO content) {

        JSONObject json =
                new JSONObject();

        putNullable(
                json,
                JSON_TMDB_ID,
                content.getTmdbId()
        );

        putNullable(
                json,
                "contentType",
                content.getContentType()
        );

        putNullable(
                json,
                "title",
                content.getTitle()
        );

        putNullable(
                json,
                "originalTitle",
                content.getOriginalTitle()
        );

        putNullable(
                json,
                "posterPath",
                content.getPosterPath()
        );

        putNullable(
                json,
                "releaseDate",
                content.getReleaseDate()
        );

        /*
         * TV 콘텐츠의 최근 회차 공개일을 별도 저장합니다.
         * 영화 콘텐츠에서는 null로 기록됩니다.
         */
        putNullable(
                json,
                "lastAirDate",
                content.getLastAirDate()
        );

        putNullable(
                json,
                "genreText",
                content.getGenreText()
        );

        putNullable(
                json,
                "ageRating",
                content.getAgeRating()
        );

        /*
         * "등급 정보 없음" 콘텐츠의 추가 재조회 상태를 함께 저장합니다.
         * 기존 JSONL에는 두 키가 없어도 정상 복원됩니다.
         */
        putNullable(
                json,
                JSON_AGE_RATING_RETRY_COUNT,
                content.getAgeRatingRetryCount()
        );

        putNullable(
                json,
                "ageRatingLastCheckedAt",
                content.getAgeRatingLastCheckedAt()
        );

        putNullable(
                json,
                JSON_AGE_RATING_RESTRICTION_CHECKED,
                content.getAgeRatingRestrictionChecked()
        );

        /*
         * 영화 러닝타임 또는 TV 대표 회차 러닝타임을
         * 분 단위로 스냅샷에 저장합니다.
         */
        putNullable(
                json,
                JSON_RUNTIME,
                content.getRuntime()
        );

        putNullable(
                json,
                JSON_EPISODE_COUNT,
                content.getEpisodeCount()
        );

        putNullable(
                json,
                "director",
                content.getDirector()
        );

        putNullable(
                json,
                "castNames",
                content.getCastNames()
        );

        putNullable(
                json,
                JSON_TMDB_SCORE,
                content.getTmdbScore()
        );

        putNullable(
                json,
                JSON_POPULARITY,
                content.getPopularity()
        );

        putNullable(
                json,
                "searchText",
                content.getSearchText()
        );

        JSONArray platformKeys =
                new JSONArray();

        if (content.getPlatformKeys() != null) {

            for (String platformKey
                    : content.getPlatformKeys()) {

                if (platformKey != null
                        && !platformKey.isBlank()) {

                    platformKeys.put(
                            platformKey
                    );
                }
            }
        }

        json.put(
                "platformKeys",
                platformKeys
        );

        return json;
    }

    /**
     * JSONL 한 줄을 CachedContentVO로 복원합니다.
     */
    private CachedContentVO fromJson(
            JSONObject json) {

        CachedContentVO content =
                new CachedContentVO();

        restoreTmdbId(json, content);

        content.setContentType(
                nullableString(
                        json,
                        "contentType"
                )
        );

        content.setTitle(
                nullableString(
                        json,
                        "title"
                )
        );

        content.setOriginalTitle(
                nullableString(
                        json,
                        "originalTitle"
                )
        );

        content.setPosterPath(
                nullableString(
                        json,
                        "posterPath"
                )
        );

        content.setReleaseDate(
                nullableString(
                        json,
                        "releaseDate"
                )
        );

        /*
         * 이전 스냅샷에는 lastAirDate가 없을 수 있으므로
         * nullableString으로 안전하게 복원합니다.
         */
        content.setLastAirDate(
                nullableString(
                        json,
                        "lastAirDate"
                )
        );

        content.setGenreText(
                nullableString(
                        json,
                        "genreText"
                )
        );

        content.setAgeRating(
                nullableString(
                        json,
                        "ageRating"
                )
        );

        /*
         * 기존 JSONL에는 등급 재조회 상태 키가 없으므로
         * 존재하는 경우에만 복원합니다.
         */
        restoreAgeRatingRetryState(json, content);

        /*
         * 기존 스냅샷에는 runtime 키가 없을 수 있으므로
         * 값이 존재할 때만 복원합니다.
         */
        restoreRuntimeData(json, content);

        content.setDirector(
                nullableString(
                        json,
                        "director"
                )
        );

        content.setCastNames(
                nullableString(
                        json,
                        "castNames"
                )
        );

        restoreScoreData(json, content);

        content.setSearchText(
                nullableString(
                        json,
                        "searchText"
                )
        );

        content.setPlatformKeys(
                readPlatformKeys(json)
        );

        return content;
    }

    private void restoreTmdbId(
            JSONObject json,
            CachedContentVO content) {

        if (json.has(JSON_TMDB_ID)
                && !json.isNull(JSON_TMDB_ID)) {

            content.setTmdbId(
                    json.getLong(
                            JSON_TMDB_ID
                    )
            );
        }
    }

    private void restoreAgeRatingRetryState(
            JSONObject json,
            CachedContentVO content) {

        if (json.has(JSON_AGE_RATING_RETRY_COUNT)
                && !json.isNull(JSON_AGE_RATING_RETRY_COUNT)) {

            content.setAgeRatingRetryCount(
                    json.getInt(
                            JSON_AGE_RATING_RETRY_COUNT
                    )
            );
        }

        content.setAgeRatingLastCheckedAt(
                nullableString(
                        json,
                        "ageRatingLastCheckedAt"
                )
        );

        if (json.has(JSON_AGE_RATING_RESTRICTION_CHECKED)
                && !json.isNull(JSON_AGE_RATING_RESTRICTION_CHECKED)) {

            content.setAgeRatingRestrictionChecked(
                    json.getBoolean(
                            JSON_AGE_RATING_RESTRICTION_CHECKED
                    )
            );
        }
    }

    private void restoreRuntimeData(
            JSONObject json,
            CachedContentVO content) {

        if (json.has(JSON_RUNTIME)
                && !json.isNull(JSON_RUNTIME)) {

            content.setRuntime(
                    json.getInt(
                            JSON_RUNTIME
                    )
            );
        }

        if (json.has(JSON_EPISODE_COUNT)
                && !json.isNull(JSON_EPISODE_COUNT)) {

            content.setEpisodeCount(
                    json.getInt(
                            JSON_EPISODE_COUNT
                    )
            );
        }
    }

    private void restoreScoreData(
            JSONObject json,
            CachedContentVO content) {

        if (json.has(JSON_TMDB_SCORE)
                && !json.isNull(JSON_TMDB_SCORE)) {

            content.setTmdbScore(
                    json.getDouble(
                            JSON_TMDB_SCORE
                    )
            );
        }

        if (json.has(JSON_POPULARITY)
                && !json.isNull(JSON_POPULARITY)) {

            content.setPopularity(
                    json.getDouble(
                            JSON_POPULARITY
                    )
            );
        }
    }

    private List<String> readPlatformKeys(
            JSONObject json) {

        JSONArray platformKeys =
                json.optJSONArray(
                        "platformKeys"
                );

        List<String> platforms =
                new ArrayList<String>();

        if (platformKeys == null) {
            return platforms;
        }

        for (int index = 0;
             index < platformKeys.length();
             index++) {

            String platformKey =
                    platformKeys.optString(
                            index,
                            null
                    );

            if (platformKey != null
                    && !platformKey.isBlank()) {

                platforms.add(
                        platformKey
                );
            }
        }

        return platforms;
    }

    /**
     * null 값을 JSONObject.NULL로 저장합니다.
     */
    private void putNullable(
            JSONObject json,
            String key,
            Object value) {

        json.put(
                key,
                value == null
                        ? JSONObject.NULL
                        : value
        );
    }

    private String nullableString(
            JSONObject json,
            String key) {

        if (!json.has(key)
                || json.isNull(key)) {

            return null;
        }

        String value =
                json.optString(
                        key,
                        null
                );

        return value == null
                || value.isBlank()
                ? null
                : value;
    }
}
