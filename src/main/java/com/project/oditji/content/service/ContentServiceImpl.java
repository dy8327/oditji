package com.project.oditji.content.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.content.dao.ContentDAO;
import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.content.vo.PersonFilmographyVO;
import com.project.oditji.tmdb.service.TmdbService;
import com.project.oditji.tmdb.vo.ActorVO;
import com.project.oditji.tmdb.vo.DirectorVO;

@Service
public class ContentServiceImpl implements ContentService {

    private final ContentDAO contentDAO;
    private final TmdbService tmdbService;

    public ContentServiceImpl(ContentDAO contentDAO, TmdbService tmdbService) {
        this.contentDAO = contentDAO;
        this.tmdbService = tmdbService;
    }

    @Override
    @Transactional
    public int prepareContentDetail(Long tmdbId, String contentType) {

        if (tmdbId == null || contentType == null || contentType.trim().isEmpty()) {
            throw new IllegalArgumentException("콘텐츠 상세 진입에 필요한 값이 없습니다.");
        }

        String normalizedType = contentType.trim().toUpperCase();

        if (!"MOVIE".equals(normalizedType) && !"TV".equals(normalizedType)) {
            throw new IllegalArgumentException("지원하지 않는 콘텐츠 타입입니다: " + normalizedType);
        }

        ContentVO existingContent = contentDAO.selectContentByTmdbId(tmdbId, normalizedType);

        if (existingContent != null) {

            tmdbService.saveContentPlatform(existingContent);
            tmdbService.saveContentPeople(existingContent);

            contentDAO.increaseViewCount(existingContent.getContentNo());

            return existingContent.getContentNo();
        }

        ContentVO contentForSave = tmdbService.getDetailForSave(tmdbId, normalizedType);

        contentDAO.insertContent(contentForSave);

        ContentVO savedContent = contentDAO.selectContentByTmdbId(tmdbId, normalizedType);

        if (savedContent == null) {
            throw new IllegalStateException("콘텐츠 저장 후 조회에 실패했습니다.");
        }

        tmdbService.saveContentPlatform(savedContent);
        tmdbService.saveContentPeople(savedContent);

        contentDAO.increaseViewCount(savedContent.getContentNo());

        return savedContent.getContentNo();
    }

    @Override
    public ContentVO getContentDetail(int contentNo) {
        return contentDAO.selectContentByContentNo(contentNo);
    }

    @Override
    public List<ActorVO> getActorListByContentNo(int contentNo) {

        List<ActorVO> actorList =
                contentDAO.selectActorListByContentNo(contentNo);

        return actorList == null
                ? Collections.emptyList()
                : actorList;
    }

    @Override
    public List<DirectorVO> getDirectorListByContentNo(int contentNo) {

        List<DirectorVO> directorList =
                contentDAO.selectDirectorListByContentNo(contentNo);

        return directorList == null
                ? Collections.emptyList()
                : directorList;
    }

    @Override
    public PersonFilmographyVO getPersonFilmography(
            Long tmdbPersonId,
            String role) {

        return tmdbService.getPersonFilmography(tmdbPersonId, role);
    }

    // 메인 페이지 콘텐츠 리스트 조회 처리 (DAO 호출 및 정렬 기준 적용)
    @Override
    public List<ContentVO> getMainContentList() {
        return contentDAO.selectMainContentList();
    }

    @Override
    public List<ContentVO> getContentListByType(String type, int page) {

        int safePage = page <= 0 ? 1 : page;
        int pageSize = 20;
        int offset = (safePage - 1) * pageSize;

        Map<String, Object> param = new HashMap<>();
        param.put("type", type);
        param.put("offset", offset);
        param.put("size", pageSize);

        return contentDAO.selectContentListByType(param);
    }
}
