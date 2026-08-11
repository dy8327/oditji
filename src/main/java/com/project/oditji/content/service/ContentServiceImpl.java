package com.project.oditji.content.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.content.dao.ContentDAO;
import com.project.oditji.content.vo.ContentListPageVO;
import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.content.vo.ContentViewHistoryVO;
import com.project.oditji.content.vo.PersonFilmographyVO;
import com.project.oditji.search.service.SearchContentPageCacheService;
import com.project.oditji.search.service.SearchContentStore;
import com.project.oditji.search.vo.CachedContentVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.service.TmdbService;
import com.project.oditji.tmdb.vo.ActorVO;
import com.project.oditji.tmdb.vo.DirectorVO;
import com.project.oditji.tmdb.vo.OttPlatformVO;

/**
 * 콘텐츠 관련 비즈니스 로직 구현체입니다.
 *
 * 기존 콘텐츠 상세 준비, 목록, 관련 콘텐츠 기능과 함께
 * 로그인 회원의 상세페이지 조회 이력을 관리합니다.
 */
@Service
public class ContentServiceImpl implements ContentService {

    private final ContentDAO contentDAO;
    private final TmdbService tmdbService;

    /*
     * 콘텐츠 목록 화면은 외부 TMDB API가 아니라
     * JSONL에 적재된 공용 검색 캐시를 사용합니다.
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
     * 검색 결과에서 상세페이지로 진입할 때 콘텐츠 데이터를 DB에 준비하고
     * 기존 CONTENT.VIEW_COUNT를 증가시킵니다.
     */
    @Override
    @Transactional
    public int prepareContentDetail(
            Long tmdbId,
            String contentType) {

        int contentNo = ensureContentStoredInternal(
                tmdbId,
                contentType
        );

        contentDAO.increaseViewCount(
                contentNo
        );

        return contentNo;
    }

    /**
     * JSONL에서 선택한 콘텐츠를 DB에 준비합니다.
     *
     * 저장 또는 보완 대상은 다음과 같습니다.
     * - CONTENT
     * - ACTOR
     * - DIRECTOR
     * - CONTENT_ACTOR
     * - CONTENT_DIRECTOR
     * - CONTENT_PLATFORM
     *
     * 상품 등록 준비 용도이므로 CONTENT.VIEW_COUNT는 증가시키지 않습니다.
     */
    @Override
    @Transactional
    public int ensureContentStored(
            Long tmdbId,
            String contentType) {
        return ensureContentStoredInternal(
                tmdbId,
                contentType
        );
    }

    private int ensureContentStoredInternal(
            Long tmdbId,
            String contentType) {

        if (tmdbId == null
                || tmdbId <= 0
                || contentType == null
                || contentType.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "콘텐츠 저장에 필요한 값이 없습니다."
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

            boolean changed =
                    applyCachedContent(
                            existingContent,
                            cachedContent
                    );

            if (changed) {
                contentDAO.updateContentFromSearchCache(
                        existingContent
                );
            }

            tmdbService.saveContentPlatform(
                    existingContent,
                    cachedContent == null
                            ? Collections.emptyList()
                            : cachedContent.getPlatformKeys()
            );

            tmdbService.saveContentPeople(
                    existingContent
            );

            return existingContent.getContentNo();
        }

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

        tmdbService.saveContentPlatform(
                savedContent,
                cachedContent == null
                        ? Collections.emptyList()
                        : cachedContent.getPlatformKeys()
        );

        tmdbService.saveContentPeople(
                savedContent
        );

        return savedContent.getContentNo();
    }

    /**
     * 로그인 회원의 콘텐츠 상세페이지 조회 이력을 저장합니다.
     *
     * CONTENT_VIEW_HISTORY는 회원·콘텐츠·날짜별로
     * 한 행만 유지합니다.
     *
     * 동일 날짜의 기록이 이미 있으면
     * Mapper의 MERGE 문이 VIEW_COUNT를 증가시킵니다.
     */
    @Override
    @Transactional
    public void recordContentViewHistory(
            Long memberNo,
            int contentNo) {

        if (memberNo == null
                || memberNo <= 0) {

            return;
        }

        if (contentNo <= 0) {

            throw new IllegalArgumentException(
                    "올바르지 않은 콘텐츠 번호입니다."
            );
        }

        ContentViewHistoryVO historyVO =
                new ContentViewHistoryVO();

        historyVO.setMemberNo(memberNo);
        historyVO.setContentNo(contentNo);

        contentDAO.mergeContentViewHistory(
                historyVO
        );
    }

    /**
     * 오늘을 포함한 최근 30일을 초과한
     * 콘텐츠 조회 이력을 삭제합니다.
     */
    @Override
    @Transactional
    public int deleteExpiredContentViewHistory() {

        return contentDAO
                .deleteExpiredContentViewHistory();
    }

    /**
     * 로그인 회원의 최근 조회 콘텐츠를
     * "최근 본 콘텐츠" 슬라이더용으로 조회합니다.
     */
    @Override
    public List<SearchResultVO> getRecentlyViewedContentList(
            Long memberNo,
            int limit) {

        if (memberNo == null
                || memberNo <= 0
                || limit <= 0) {

            return Collections.emptyList();
        }

        Map<String, Object> param =
                new HashMap<String, Object>();

        param.put("memberNo", memberNo);
        param.put("limit", limit);

        List<ContentVO> recentContentList =
                contentDAO.selectRecentViewedContentList(
                        param
                );

        if (recentContentList == null
                || recentContentList.isEmpty()) {

            return Collections.emptyList();
        }

        return searchContentPageCacheService
                .getMainRecentlyViewedContent(
                        recentContentList
                );
    }

    /**
     * "출시 알림 캘린더"용 콘텐츠 목록을 JSONL 공용 캐시에서 조회합니다.
     */
    @Override
    public List<SearchResultVO> getReleaseCalendarContent(
            int year,
            int month) {

        return searchContentPageCacheService
                .getReleaseCalendarContent(
                        year,
                        month
                );
    }

    /**
     * 검색 JSON 캐시 값을 DB 저장용 ContentVO에 반영합니다.
     *
     * 캐시에 값이 있을 때만 반영하므로
     * TMDB 상세 API의 정상값을 null로 덮어쓰지 않습니다.
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
     * 콘텐츠 상세 페이지의 관련 콘텐츠를
     * JSONL 공용 캐시에서 조회합니다.
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
            String sort,
            int page,
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds) {

        /*
         * 기존 3개 목록 필터를 사용하는 호출부와의 호환성을 유지합니다.
         * 네 번째 필터가 없는 경우 빈 목록을 전달합니다.
         */
        return getContentListByType(
                type,
                sort,
                page,
                contentCategories,
                genreCodes,
                providerIds,
                Collections.emptyList()
        );
    }

    @Override
    public ContentListPageVO getContentListByType(
            String type,
            String sort,
            int page,
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds,
            List<String> additionalFilterValues) {

        return searchContentPageCacheService
                .getContentListPage(
                        type,
                        sort,
                        page,
                        contentCategories,
                        genreCodes,
                        providerIds,
                        additionalFilterValues
                );
    }

    @Override
    public List<SearchResultVO> getContentRecommendedList(
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds) {

        /*
         * 기존 3개 목록 필터를 사용하는 호출부와의 호환성을 유지합니다.
         * 네 번째 필터가 없는 경우 빈 목록을 전달합니다.
         */
        return getContentRecommendedList(
                contentCategories,
                genreCodes,
                providerIds,
                Collections.emptyList()
        );
    }

    @Override
    public List<SearchResultVO> getContentRecommendedList(
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds,
            List<String> additionalFilterValues) {

        return searchContentPageCacheService
                .getContentRecommendedList(
                        contentCategories,
                        genreCodes,
                        providerIds,
                        additionalFilterValues,
                        5
                );
    }
}