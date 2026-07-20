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

    @Value("${search.content-cache.snapshot-enabled:true}")
    private boolean snapshotEnabled;

    @Value("${search.content-cache.snapshot-path:C:/oditji/cache/search-content-cache.jsonl}")
    private String snapshotPath;

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

                try {

                    result.add(
                            fromJson(
                                    new JSONObject(line)
                            )
                    );

                } catch (RuntimeException e) {

                    /*
                     * 특정 JSONL 한 줄이 손상되어도
                     * 전체 스냅샷 복원을 중단하지 않습니다.
                     */
                    System.err.println(
                            "검색 콘텐츠 스냅샷 일부 복원 실패: "
                                    + e.getMessage()
                    );
                }
            }

        } catch (IOException e) {

            throw new IllegalStateException(
                    "검색 콘텐츠 스냅샷을 읽지 못했습니다.",
                    e
            );
        }

        return result;
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

                        if (content == null) {
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
                "tmdbId",
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

        putNullable(
                json,
                "episodeCount",
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
                "tmdbScore",
                content.getTmdbScore()
        );

        putNullable(
                json,
                "popularity",
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

        if (json.has("tmdbId")
                && !json.isNull("tmdbId")) {

            content.setTmdbId(
                    json.getLong(
                            "tmdbId"
                    )
            );
        }

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

        if (json.has("episodeCount")
                && !json.isNull("episodeCount")) {

            content.setEpisodeCount(
                    json.getInt(
                            "episodeCount"
                    )
            );
        }

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

        if (json.has("tmdbScore")
                && !json.isNull("tmdbScore")) {

            content.setTmdbScore(
                    json.getDouble(
                            "tmdbScore"
                    )
            );
        }

        if (json.has("popularity")
                && !json.isNull("popularity")) {

            content.setPopularity(
                    json.getDouble(
                            "popularity"
                    )
            );
        }

        content.setSearchText(
                nullableString(
                        json,
                        "searchText"
                )
        );

        JSONArray platformKeys =
                json.optJSONArray(
                        "platformKeys"
                );

        List<String> platforms =
                new ArrayList<String>();

        if (platformKeys != null) {

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
        }

        content.setPlatformKeys(
                platforms
        );

        return content;
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