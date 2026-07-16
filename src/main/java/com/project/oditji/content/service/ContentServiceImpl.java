package com.project.oditji.content.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.content.dao.ContentDAO;
import com.project.oditji.content.vo.ContentListPageVO;
import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.content.vo.PersonFilmographyVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.service.TmdbService;
import com.project.oditji.tmdb.vo.ActorVO;
import com.project.oditji.tmdb.vo.DirectorVO;
import com.project.oditji.tmdb.vo.OttPlatformVO;

@Service
public class ContentServiceImpl implements ContentService {

    private final ContentDAO contentDAO;
    private final TmdbService tmdbService;
    private final ContentListTmdbService contentListTmdbService;

    public ContentServiceImpl(
            ContentDAO contentDAO,
            TmdbService tmdbService,
            ContentListTmdbService contentListTmdbService) {

        this.contentDAO = contentDAO;
        this.tmdbService = tmdbService;
        this.contentListTmdbService = contentListTmdbService;
    }

    @Override
    @Transactional
    public int prepareContentDetail(
            Long tmdbId,
            String contentType) {

        if (tmdbId == null
                || contentType == null
                || contentType.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "콘텐츠 상세 진입에 필요한 값이 없습니다.");
        }

        String normalizedType =
                contentType.trim().toUpperCase();

        if (!"MOVIE".equals(normalizedType)
                && !"TV".equals(normalizedType)) {

            throw new IllegalArgumentException(
                    "지원하지 않는 콘텐츠 타입입니다: "
                    + normalizedType);
        }

        ContentVO existingContent =
                contentDAO.selectContentByTmdbId(
                        tmdbId,
                        normalizedType);

        if (existingContent != null) {

            tmdbService.saveContentPlatform(
                    existingContent);

            tmdbService.saveContentPeople(
                    existingContent);

            contentDAO.increaseViewCount(
                    existingContent.getContentNo());

            return existingContent.getContentNo();
        }

        ContentVO contentForSave =
                tmdbService.getDetailForSave(
                        tmdbId,
                        normalizedType);

        contentDAO.insertContent(
                contentForSave);

        ContentVO savedContent =
                contentDAO.selectContentByTmdbId(
                        tmdbId,
                        normalizedType);

        if (savedContent == null) {

            throw new IllegalStateException(
                    "콘텐츠 저장 후 조회에 실패했습니다.");
        }

        tmdbService.saveContentPlatform(
                savedContent);

        tmdbService.saveContentPeople(
                savedContent);

        contentDAO.increaseViewCount(
                savedContent.getContentNo());

        return savedContent.getContentNo();
    }

    @Override
    public ContentVO getContentDetail(int contentNo) {
        return contentDAO.selectContentByContentNo(
                contentNo);
    }

    @Override
    public List<ActorVO> getActorListByContentNo(
            int contentNo) {

        List<ActorVO> actorList =
                contentDAO.selectActorListByContentNo(
                        contentNo);

        return actorList == null
                ? Collections.emptyList()
                : actorList;
    }

    @Override
    public List<DirectorVO> getDirectorListByContentNo(
            int contentNo) {

        List<DirectorVO> directorList =
                contentDAO.selectDirectorListByContentNo(
                        contentNo);

        return directorList == null
                ? Collections.emptyList()
                : directorList;
    }

    @Override
    public List<OttPlatformVO> getOttPlatformListByContentNo(
            int contentNo) {

        List<OttPlatformVO> ottList =
                contentDAO.selectOttPlatformListByContentNo(
                        contentNo);

        return ottList == null
                ? Collections.emptyList()
                : ottList;
    }

    @Override
    public List<ContentVO> getRelatedContentList(
            int contentNo) {

        ContentVO currentContent =
                contentDAO.selectContentByContentNo(contentNo);

        if (currentContent == null) {
            return Collections.emptyList();
        }

        Map<String, Object> param =
                new HashMap<String, Object>();

        param.put("contentNo", contentNo);
        param.put("candidateSize", 500);

        List<ContentVO> candidates =
                contentDAO.selectRelatedContentCandidates(param);

        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }

        final String currentCategory =
                resolveRecommendationCategory(currentContent);

        final List<String> currentGenreList =
                splitNormalizedList(currentContent.getGenreText());

        final Set<String> currentGenres =
                new HashSet<String>(currentGenreList);

        final String currentMainGenre =
                resolveMainGenre(currentGenreList);

        final Set<String> currentCast =
                splitNormalizedValues(currentContent.getCastNames());

        final Set<String> currentDirectors =
                splitNormalizedValues(currentContent.getDirector());

        List<ContentVO> sameCategoryCandidates =
                new ArrayList<ContentVO>();

        for (ContentVO candidate : candidates) {

            if (currentCategory.equals(
                    resolveRecommendationCategory(candidate))) {

                sameCategoryCandidates.add(candidate);
            }
        }

        if (sameCategoryCandidates.isEmpty()) {
            return Collections.emptyList();
        }

        sameCategoryCandidates.sort(
                Comparator
                        .comparingInt(
                                (ContentVO candidate) ->
                                        calculateRelatedScore(
                                                candidate,
                                                currentGenres,
                                                currentMainGenre,
                                                currentCast,
                                                currentDirectors))
                        .reversed()
                        .thenComparing(
                                ContentVO::getTmdbScore,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()))
                        .thenComparing(
                                ContentVO::getViewCount,
                                Comparator.reverseOrder())
                        .thenComparing(
                                ContentVO::getContentNo,
                                Comparator.reverseOrder()));

        int resultSize =
                Math.min(3, sameCategoryCandidates.size());

        return new ArrayList<ContentVO>(
                sameCategoryCandidates.subList(
                        0,
                        resultSize));
    }

    private int calculateRelatedScore(
            ContentVO candidate,
            Set<String> currentGenres,
            String currentMainGenre,
            Set<String> currentCast,
            Set<String> currentDirectors) {

        int score = 0;

        List<String> candidateGenreList =
                splitNormalizedList(candidate.getGenreText());

        Set<String> candidateGenres =
                new HashSet<String>(candidateGenreList);

        String candidateMainGenre =
                resolveMainGenre(candidateGenreList);

        if (!currentMainGenre.isEmpty()
                && currentMainGenre.equals(candidateMainGenre)) {

            score += 1000;
        }

        for (String genre : candidateGenres) {

            if (currentGenres.contains(genre)) {
                score += 50;
            }
        }

        Set<String> candidateDirectors =
                splitNormalizedValues(candidate.getDirector());

        for (String director : candidateDirectors) {

            if (currentDirectors.contains(director)) {
                score += 30;
            }
        }

        Set<String> candidateCast =
                splitNormalizedValues(candidate.getCastNames());

        for (String castName : candidateCast) {

            if (currentCast.contains(castName)) {
                score += 10;
            }
        }

        return score;
    }

    private String resolveRecommendationCategory(
            ContentVO content) {

        if (content == null) {
            return "";
        }

        String contentType =
                normalizeValue(content.getContentType());

        if ("movie".equals(contentType)) {
            return "MOVIE";
        }

        List<String> genres =
                splitNormalizedList(content.getGenreText());

        if (genres.contains("애니메이션")) {
            return "ANIMATION";
        }

        if (genres.contains("리얼리티")
                || genres.contains("토크")) {

            return "ENTERTAINMENT";
        }

        if (genres.contains("다큐멘터리")) {
            return "DOCUMENTARY";
        }

        return "DRAMA";
    }

    private String resolveMainGenre(
            List<String> genres) {

        if (genres == null || genres.isEmpty()) {
            return "";
        }

        Set<String> excludedGenres =
                new HashSet<String>();

        Collections.addAll(
                excludedGenres,
                "드라마",
                "애니메이션",
                "다큐멘터리",
                "리얼리티",
                "토크",
                "연속극",
                "키즈",
                "tv영화",
                "뉴스");

        for (String genre : genres) {

            String comparisonValue =
                    genre.replace(" ", "");

            if (!excludedGenres.contains(comparisonValue)) {
                return genre;
            }
        }

        return genres.get(0);
    }

    private List<String> splitNormalizedList(
            String value) {

        List<String> result =
                new ArrayList<String>();

        if (value == null || value.trim().isEmpty()) {
            return result;
        }

        String[] tokens = value.split(",");

        for (String token : tokens) {

            String normalized =
                    normalizeValue(token);

            if (!normalized.isEmpty()
                    && !result.contains(normalized)) {

                result.add(normalized);
            }
        }

        return result;
    }

    private Set<String> splitNormalizedValues(
            String value) {

        return new HashSet<String>(
                splitNormalizedList(value));
    }

    private String normalizeValue(
            String value) {

        return value == null
                ? ""
                : value.trim().toLowerCase(Locale.ROOT);
    }

    @Override
    public PersonFilmographyVO getPersonFilmography(
            Long tmdbPersonId,
            String role) {

        return tmdbService.getPersonFilmography(
                tmdbPersonId,
                role);
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

        return contentListTmdbService.getContentListPage(
                type,
                page,
                contentCategories,
                genreCodes,
                providerIds);
    }

    @Override
    public List<SearchResultVO> getContentRecommendedList(
            List<String> providerIds) {

        return contentListTmdbService.getRecommendedList(
                providerIds);
    }
}