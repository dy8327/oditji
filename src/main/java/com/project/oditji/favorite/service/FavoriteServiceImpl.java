package com.project.oditji.favorite.service;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.favorite.dao.FavoriteDAO;
import com.project.oditji.favorite.vo.FavoriteReleaseTargetVO;
import com.project.oditji.favorite.vo.FavoriteVO;
import com.project.oditji.notification.service.NotificationService;

@Service
public class FavoriteServiceImpl
        implements FavoriteService {

    private static final String NOTIFICATION_TYPE_CONTENT_RELEASE = "CONTENT_RELEASE";
    private static final String REFERENCE_TYPE_CONTENT = "CONTENT";
    private static final String CONTENT_DETAIL_URL_PREFIX = "/content/prepare?tmdbId=";

    private final FavoriteDAO favoriteDAO;
    private final NotificationService notificationService;

    public FavoriteServiceImpl(
            FavoriteDAO favoriteDAO,
            NotificationService notificationService) {

        this.favoriteDAO = favoriteDAO;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public boolean toggleFavorite(
            FavoriteVO favoriteVO) {

        validateFavorite(favoriteVO);

        int count =
                favoriteDAO.countFavorite(
                        favoriteVO);

        if (count > 0) {

            favoriteDAO.deleteFavorite(
                    favoriteVO);

            return false;
        }

        favoriteDAO.insertFavorite(
                favoriteVO);

        return true;
    }

    @Override
    public boolean isFavorite(
            FavoriteVO favoriteVO) {

        if (favoriteVO == null
                || favoriteVO.getMemberNo() == null
                || favoriteVO.getContentNo() == null) {

            return false;
        }

        return favoriteDAO.countFavorite(
                favoriteVO) > 0;
    }

    @Override
    public boolean isFavoriteByTmdb(
            FavoriteVO favoriteVO) {

        if (favoriteVO == null
                || favoriteVO.getMemberNo() == null
                || favoriteVO.getTmdbId() == null
                || favoriteVO.getTmdbId() <= 0
                || favoriteVO.getContentType() == null
                || favoriteVO.getContentType().isBlank()) {

            return false;
        }

        favoriteVO.setContentType(
                normalizeContentType(
                        favoriteVO.getContentType()));

        return favoriteDAO.countFavoriteByTmdb(
                favoriteVO) > 0;
    }

    @Override
        public int getFavoriteCount(Long memberNo) {

        if (memberNo == null) {
                return 0;
        }

        return favoriteDAO.countFavoriteByMemberNo(memberNo);
        }

    @Override
    public List<ContentVO> selectFavoriteList(
            Long memberNo) {

        if (memberNo == null) {
            return Collections.emptyList();
        }

        List<ContentVO> favoriteList =
                favoriteDAO.selectFavoriteList(
                        memberNo);

        return favoriteList == null
                ? Collections.emptyList()
                : favoriteList;
    }

    /**
     * 오늘 또는 내일 개봉·공개하는 콘텐츠를 찜한 회원 전원에게 알림을 생성합니다.
     *
     * DAYS_UNTIL_RELEASE가 0(오늘 공개)인지 1(내일 공개, 하루 전)인지에 따라
     * 알림 제목·문구를 다르게 보냅니다. 값이 없거나 0이 아닌 경우에는
     * 하루 전 알림 문구를 기본값으로 사용합니다.
     */
    @Override
    @Transactional
    public int notifyUpcomingReleases() {

        List<FavoriteReleaseTargetVO> targetList =
                favoriteDAO.selectFavoriteReleaseTargetList();

        if (targetList == null
                || targetList.isEmpty()) {

            return 0;
        }

        int notifiedCount = 0;

        for (FavoriteReleaseTargetVO target : targetList) {

            if (target == null
                    || target.getMemberNo() == null) {

                continue;
            }

            boolean isReleasedToday =
                    target.getDaysUntilRelease() != null
                            && target.getDaysUntilRelease() == 0;

            String title = isReleasedToday
                    ? "찜한 콘텐츠가 오늘 공개돼요"
                    : "찜한 콘텐츠가 내일 공개돼요";

            String message = isReleasedToday
                    ? "'" + target.getTitle()
                            + "'가 오늘 공개됩니다. 지금 만나보세요!"
                    : "'" + target.getTitle()
                            + "'가 내일 공개됩니다. 놓치지 마세요!";

            notificationService.createForMember(
                    target.getMemberNo(),
                    NOTIFICATION_TYPE_CONTENT_RELEASE,
                    title,
                    message,
                    CONTENT_DETAIL_URL_PREFIX
                            + target.getTmdbId()
                            + "&contentType="
                            + target.getContentType(),
                    REFERENCE_TYPE_CONTENT,
                    target.getContentNo());

            notifiedCount++;
        }

        return notifiedCount;
    }

    private void validateFavorite(
            FavoriteVO favoriteVO) {

        if (favoriteVO == null) {
            throw new IllegalArgumentException(
                    "찜 정보가 없습니다.");
        }

        if (favoriteVO.getMemberNo() == null) {
            throw new IllegalArgumentException(
                    "회원 정보가 없습니다.");
        }

        if (favoriteVO.getContentNo() == null) {
            throw new IllegalArgumentException(
                    "콘텐츠 정보가 없습니다.");
        }
    }

    private String normalizeContentType(
            String contentType) {

        String normalized =
                contentType.trim()
                        .toUpperCase(Locale.ROOT);

        if (!"MOVIE".equals(normalized)
                && !"TV".equals(normalized)) {

            throw new IllegalArgumentException(
                    "올바르지 않은 콘텐츠 유형입니다.");
        }

        return normalized;
    }
}