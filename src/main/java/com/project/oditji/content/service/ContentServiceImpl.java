package com.project.oditji.content.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.content.dao.ContentDAO;
import com.project.oditji.content.vo.ContentListPageVO;
import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.content.vo.PersonFilmographyVO;
import com.project.oditji.search.service.SearchContentPageCacheService;
import com.project.oditji.search.service.SearchContentStore;
import com.project.oditji.search.vo.CachedContentVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.service.TmdbService;
import com.project.oditji.tmdb.vo.ActorVO;
import com.project.oditji.tmdb.vo.DirectorVO;
import com.project.oditji.tmdb.vo.OttPlatformVO;

@Service
public class ContentServiceImpl implements ContentService {

    private final ContentDAO contentDAO;
    private final TmdbService tmdbService;
    /*
     * 콘텐츠 목록 화면은 외부 TMDB API가 아니라
     * JSONL에서 적재된 공용 검색 캐시를 사용합니다.
     */
    private final SearchContentPageCacheService
            searchContentPageCacheService;

    private final SearchContentStore searchContentStore;

    public ContentServiceImpl(
            ContentDAO contentDAO,
            TmdbService tmdbService,
            SearchContentPageCacheService
                    searchContentPageCacheService,
            SearchContentStore searchContentStore) {

        this.contentDAO = contentDAO;
        this.tmdbService = tmdbService;
        this.searchContentPageCacheService =
                searchContentPageCacheService;
        this.searchContentStore =
                searchContentStore;
    }

    /**
     * 검색 결과에서 상세페이지로 진입할 때 콘텐츠를 DB에 준비합니다.
     *
     * 기존 콘텐츠:
     * - JSON 공용 캐시 값을 기존 CONTENT 행에 UPDATE
     * - 플랫폼과 인물 관계 저장
     * - 조회수 증가
     *
     * 신규 콘텐츠:
     * - TMDB 상세 저장용 데이터를 생성
     * - JSON 공용 캐시 값을 우선 반영
     * - CONTENT INSERT
     * - 플랫폼과 인물 관계 저장
     * - 조회수 증가
     */
    @Override
    @Transactional
    public int prepareContentDetail(
            Long tmdbId,
            String contentType) {

        if (tmdbId == null
                || contentType == null
                || contentType.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "콘텐츠 상세 진입에 필요한 값이 없습니다."
            );
        }

        String normalizedType =
                contentType.trim()
                        .toUpperCase(Locale.ROOT);

        if (!"MOVIE".equals(normalizedType)
                && !"TV".equals(normalizedType)) {

            throw new IllegalArgumentException(
                    "지원하지 않는 콘텐츠 타입입니다: "
                            + normalizedType
            );
        }

        CachedContentVO cachedContent =
                searchContentStore
                        .findByTmdbIdAndContentType(
                                tmdbId,
                                normalizedType
                        );

        ContentVO existingContent =
                contentDAO.selectContentByTmdbId(
                        tmdbId,
                        normalizedType
                );

        if (existingContent != null) {

            /*
             * 검색 JSON에 저장된 장르, 평점, 연령등급 등
             * 검색 기준 데이터를 기존 DB 행에 반영합니다.
             */
            boolean changed =
                    applyCachedContent(
                            existingContent,
                            cachedContent
                    );

            if (changed) {

                contentDAO
                        .updateContentFromSearchCache(
                                existingContent
                        );
            }

            /*
             * 검색 JSON에 저장된 플랫폼 키를 우선 사용합니다.
             * TMDB watch/providers의 시점별 응답 차이로
             * Wavve 등 일부 OTT가 누락되는 문제를 방지합니다.
             */
            tmdbService.saveContentPlatform(
                    existingContent,
                    cachedContent == null
                            ? Collections.emptyList()
                            : cachedContent.getPlatformKeys()
            );

            tmdbService.saveContentPeople(
                    existingContent
            );

            contentDAO.increaseViewCount(
                    existingContent.getContentNo()
            );

            return existingContent.getContentNo();
        }

        /*
         * DB에 없는 콘텐츠는 기존 TMDB 상세 저장 로직으로
         * overview, backdrop, runtime 등의 값을 만든 뒤
         * 검색 JSON 값을 우선 적용합니다.
         */
        ContentVO contentForSave =
                tmdbService.getDetailForSave(
                        tmdbId,
                        normalizedType
                );

        applyCachedContent(
                contentForSave,
                cachedContent
        );

        contentDAO.insertContent(
                contentForSave
        );

        ContentVO savedContent =
                contentDAO.selectContentByTmdbId(
                        tmdbId,
                        normalizedType
                );

        if (savedContent == null) {

            throw new IllegalStateException(
                    "콘텐츠 저장 후 조회에 실패했습니다."
            );
        }

        /*
         * 신규 콘텐츠도 동일하게 JSON 플랫폼 키를 우선 저장합니다.
         * JSON에 플랫폼 키가 없는 경우에는 TmdbServiceImpl에서
         * 기존 TMDB API 방식으로 자동 대체합니다.
         */
        tmdbService.saveContentPlatform(
                savedContent,
                cachedContent == null
                        ? Collections.emptyList()
                        : cachedContent.getPlatformKeys()
        );

        tmdbService.saveContentPeople(
                savedContent
        );

        contentDAO.increaseViewCount(
                savedContent.getContentNo()
        );

        return savedContent.getContentNo();
    }

    /**
     * 검색 JSON 캐시 값을 DB 저장용 ContentVO에 반영합니다.
     *
     * 캐시에 값이 있을 때만 반영하므로
     * TMDB 상세 API에서 이미 가져온 정상값을 null로 덮어쓰지 않습니다.
     *
     * @return 한 개 이상의 값이 반영됐으면 true
     */
    private boolean applyCachedContent(
            ContentVO target,
            CachedContentVO cached) {

        if (target == null
                || cached == null) {

            return false;
        }

        boolean changed = false;

        if (hasText(cached.getTitle())) {
            target.setTitle(cached.getTitle());
            changed = true;
        }

        if (hasText(cached.getOriginalTitle())) {
            target.setOriginalTitle(
                    cached.getOriginalTitle()
            );
            changed = true;
        }

        if (hasText(cached.getPosterPath())) {
            target.setPosterPath(
                    cached.getPosterPath()
            );
            changed = true;
        }

        LocalDate cachedReleaseDate =
                parseLocalDate(
                        cached.getReleaseDate()
                );

        if (cachedReleaseDate != null) {
            target.setReleaseDate(
                    cachedReleaseDate
            );
            changed = true;
        }

        if (hasText(cached.getGenreText())) {
            target.setGenreText(
                    cached.getGenreText()
            );
            changed = true;
        }

        if (cached.getEpisodeCount() != null) {
            target.setEpisodeCount(
                    cached.getEpisodeCount()
            );
            changed = true;
        }

        if (hasText(cached.getDirector())) {
            target.setDirector(
                    cached.getDirector()
            );
            changed = true;
        }

        if (hasText(cached.getCastNames())) {
            target.setCastNames(
                    cached.getCastNames()
            );
            changed = true;
        }

        if (hasText(cached.getAgeRating())) {
            target.setAgeRating(
                    cached.getAgeRating()
            );
            changed = true;
        }

        if (cached.getTmdbScore() != null) {
            target.setTmdbScore(
                    cached.getTmdbScore()
            );
            changed = true;
        }

        return changed;
    }

    /**
     * JSON 문자열 날짜를 ContentVO의 LocalDate로 변환합니다.
     */
    private LocalDate parseLocalDate(
            String value) {

        if (!hasText(value)) {
            return null;
        }

        try {

            return LocalDate.parse(
                    value.trim()
            );

        } catch (DateTimeParseException e) {

            return null;
        }
    }

    private boolean hasText(
            String value) {

        return value != null
                && !value.trim().isEmpty();
    }

    @Override
    public ContentVO getContentDetail(
            int contentNo) {

        return contentDAO.selectContentByContentNo(
                contentNo
        );
    }

    @Override
    public List<ActorVO> getActorListByContentNo(
            int contentNo) {

        List<ActorVO> actorList =
                contentDAO.selectActorListByContentNo(
                        contentNo
                );

        return actorList == null
                ? Collections.emptyList()
                : actorList;
    }

    @Override
    public List<DirectorVO> getDirectorListByContentNo(
            int contentNo) {

        List<DirectorVO> directorList =
                contentDAO.selectDirectorListByContentNo(
                        contentNo
                );

        return directorList == null
                ? Collections.emptyList()
                : directorList;
    }

    @Override
    public List<OttPlatformVO> getOttPlatformListByContentNo(
            int contentNo) {

        List<OttPlatformVO> ottList =
                contentDAO.selectOttPlatformListByContentNo(
                        contentNo
                );

        return ottList == null
                ? Collections.emptyList()
                : ottList;
    }

    /**
     * 콘텐츠 상세 페이지의 관련 콘텐츠를 JSONL 공용 캐시에서 조회합니다.
     *
     * 현재 상세 콘텐츠 자체는 DB에서 조회하지만,
     * 추천 후보는 DB 적재 여부와 관계없이 JSONL 전체 콘텐츠를 사용합니다.
     */
    @Override
    public List<SearchResultVO> getRelatedContentList(
            int contentNo) {

        ContentVO currentContent =
                contentDAO.selectContentByContentNo(
                        contentNo
                );

        if (currentContent == null) {
            return Collections.emptyList();
        }

        return searchContentPageCacheService
                .getRelatedContentList(
                        currentContent,
                        3
                );
    }

    @Override
    public PersonFilmographyVO getPersonFilmography(
            Long tmdbPersonId,
            String role) {

        return tmdbService.getPersonFilmography(
                tmdbPersonId,
                role
        );
    }

    @Override
    public List<ContentVO> getMainContentList() {

        return contentDAO.selectMainContentList();
    }

    @Override
    public ContentListPageVO getContentListByType(
            String type,
            int page,
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds) {

        /*
         * 기존 ContentListTmdbService의 discover API 호출 대신
         * SearchContentStore에 적재된 JSONL 콘텐츠를 사용합니다.
         */
        return searchContentPageCacheService
                .getContentListPage(
                        type,
                        page,
                        contentCategories,
                        genreCodes,
                        providerIds
                );
    }

    @Override
    public List<SearchResultVO> getContentRecommendedList(
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds) {

        /*
         * 현재 목록 필터를 반영한 인기 콘텐츠 중
         * 상위 5개를 우측 추천 영역에 표시합니다.
         */
        return searchContentPageCacheService
                .getContentRecommendedList(
                        contentCategories,
                        genreCodes,
                        providerIds,
                        5
                );
    }
}
