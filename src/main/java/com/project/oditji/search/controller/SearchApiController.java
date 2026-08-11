package com.project.oditji.search.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.oditji.common.util.LoginMemberUtil;
import com.project.oditji.search.service.SearchContentPageCacheService;
import com.project.oditji.search.service.SearchKeywordHistoryService;
import com.project.oditji.search.vo.SearchKeywordResponseVO;
import com.project.oditji.search.vo.SearchResultVO;

import jakarta.servlet.http.HttpSession;

/**
 * 헤더 검색창의 자동완성 미리보기와
 * 로그인 회원의 최근 검색어를 제공하는 API입니다.
 *
 * 검색어 이력 저장 자체는 SearchController의 /search 요청에서
 * 부가 효과로 처리하고, 이 API는 조회·삭제만 담당합니다.
 */
@RestController
@RequestMapping("/search/api")
public class SearchApiController {

    /**
     * 드롭다운에 노출하는 자동완성 콘텐츠/배우 미리보기 최대 개수입니다.
     */
    private static final int AUTOCOMPLETE_PREVIEW_SIZE = 6;

    /**
     * 헤더 검색창에 노출하는 최근 검색어 최대 개수입니다.
     */
    private static final int RECENT_KEYWORD_SIZE = 10;

    private final SearchContentPageCacheService searchContentPageCacheService;
    private final SearchKeywordHistoryService searchKeywordHistoryService;

    public SearchApiController(
            SearchContentPageCacheService searchContentPageCacheService,
            SearchKeywordHistoryService searchKeywordHistoryService) {

        this.searchContentPageCacheService =
                searchContentPageCacheService;

        this.searchKeywordHistoryService =
                searchKeywordHistoryService;
    }

    /**
     * 검색창 타이핑 중 콘텐츠/배우 미리보기 드롭다운용 목록입니다.
     *
     * 비로그인 사용자도 이용할 수 있습니다.
     */
    @GetMapping("/autocomplete")
    public List<SearchResultVO> autocomplete(
            @RequestParam(value = "keyword", required = false) String keyword) {

        return searchContentPageCacheService.getAutocompletePreview(
                keyword,
                AUTOCOMPLETE_PREVIEW_SIZE
        );
    }

    /**
     * 로그인 회원의 최근 검색어 목록입니다.
     *
     * 비로그인 사용자는 빈 목록을 받으며,
     * 클라이언트는 이 경우 브라우저 저장소의 최근 검색어를 사용합니다.
     */
    @GetMapping("/recent-keywords")
    public List<String> recentKeywords(HttpSession session) {

        Long loginMemberNo =
                LoginMemberUtil.getLoginMemberNo(session);

        return searchKeywordHistoryService.getRecentSearchKeywordList(
                loginMemberNo,
                RECENT_KEYWORD_SIZE
        );
    }

    /**
     * 로그인 회원의 최근 검색어 중 하나를 삭제합니다.
     *
     * 검색어에 슬래시(/), 공백 등이 포함될 수 있어
     * 경로 변수 대신 쿼리 파라미터로 받습니다.
     */
    @DeleteMapping("/recent-keywords")
    public SearchKeywordResponseVO deleteRecentKeyword(
            @RequestParam("keyword") String keyword,
            HttpSession session) {

        Long loginMemberNo =
                LoginMemberUtil.getLoginMemberNo(session);

        if (loginMemberNo == null) {

            return new SearchKeywordResponseVO(
                    false,
                    "로그인 후 이용할 수 있습니다."
            );
        }

        searchKeywordHistoryService.deleteSearchKeyword(
                loginMemberNo,
                keyword
        );

        return new SearchKeywordResponseVO(
                true,
                "최근 검색어를 삭제했습니다."
        );
    }

    /**
     * 로그인 회원의 최근 검색어를 모두 삭제합니다.
     */
    @DeleteMapping("/recent-keywords/all")
    public SearchKeywordResponseVO deleteAllRecentKeywords(
            HttpSession session) {

        Long loginMemberNo =
                LoginMemberUtil.getLoginMemberNo(session);

        if (loginMemberNo == null) {

            return new SearchKeywordResponseVO(
                    false,
                    "로그인 후 이용할 수 있습니다."
            );
        }

        searchKeywordHistoryService.deleteAllSearchKeyword(
                loginMemberNo
        );

        return new SearchKeywordResponseVO(
                true,
                "최근 검색어를 모두 삭제했습니다."
        );
    }
}
