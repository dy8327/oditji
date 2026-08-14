package com.project.oditji.subscription.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.oditji.common.util.LoginMemberUtil;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.search.service.SearchContentPageCacheService;
import com.project.oditji.search.vo.SearchResultPageVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.subscription.service.SubscriptionCalculatorService;
import com.project.oditji.subscription.vo.ContentWishItemVO;
import com.project.oditji.subscription.vo.SubscriptionCalculateRequestVO;
import com.project.oditji.subscription.vo.SubscriptionCalculationResultVO;
import com.project.oditji.subscription.vo.SubscriptionSavedResultVO;
import com.project.oditji.tmdb.vo.OttPlatformVO;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/subscription")
public class SubscriptionApiController {

    /** 위시리스트에 담기 전 미리보기용 검색 결과 개수 (자동완성 성격이라 적게 유지) */
    private static final int SEARCH_RESULT_SIZE = 8;

    // [마이페이지 구독 계산 결과 모달 연동 추가]
    private static final String RESPONSE_SUCCESS = "success";
    private static final String RESPONSE_MESSAGE = "message";
    private static final String MESSAGE_LOGIN_REQUIRED = "로그인이 필요합니다.";

    private final SearchContentPageCacheService searchContentPageCacheService;

    private final SubscriptionCalculatorService subscriptionCalculatorService;

    public SubscriptionApiController(
            SearchContentPageCacheService searchContentPageCacheService,
            SubscriptionCalculatorService subscriptionCalculatorService) {

        this.searchContentPageCacheService =
                searchContentPageCacheService;

        this.subscriptionCalculatorService =
                subscriptionCalculatorService;
    }

    /**
     * 위시리스트에 담을 콘텐츠를 검색한다. 콘텐츠의 플랫폼 목록을 함께 내려줘서,
     * 프런트가 담은 항목을 그대로 /calculate 요청 본문으로 재사용할 수 있게 한다.
     */
    @GetMapping("/search-content")
    public List<ContentWishItemVO> searchContent(
            @RequestParam(required = false) String keyword) {

        if (keyword == null
                || keyword.trim().isEmpty()) {

            return Collections.emptyList();
        }

        SearchResultPageVO page =
                searchContentPageCacheService.getContentPage(
                        keyword,
                        1,
                        SEARCH_RESULT_SIZE,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList());

        List<ContentWishItemVO> wishItemList =
                new ArrayList<ContentWishItemVO>();

        if (page == null
                || page.getResultList() == null) {

            return wishItemList;
        }

        for (SearchResultVO content : page.getResultList()) {

            wishItemList.add(
                    toWishItem(content));
        }

        return wishItemList;
    }

    /**
     * 위시리스트를 가장 저렴하게 커버하는 OTT 구독 조합을 계산한다.
     * request에는 위시리스트뿐 아니라 사용자가 선택한 할인 조건(통신사/카드사/멤버십)이
     * 함께 담겨 오며, 이 조건과 일치하는 할인만 반영해 실제로 이용 가능한 가격을 산출한다.
     */
    @PostMapping("/calculate")
    public SubscriptionCalculationResultVO calculate(
            @RequestBody SubscriptionCalculateRequestVO request) {

        return subscriptionCalculatorService.calculate(
                request.getWishItemList(),
                request.getTelecomCode(),
                request.getCardCompany(),
                request.getMembershipName());
    }

    /**
     * 계산 결과를 저장하고 공유 링크(/subscription/result/{resultId})에 쓸
     * resultId를 발급한다. 로그인 사용자는 세션의 loginMember를 MEMBER_NO로 매핑하고,
     * 비로그인 사용자도 저장할 수 있다.
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> save(
            @RequestBody SubscriptionCalculationResultVO result,
            HttpSession session) {

        try {

            MemberVO loginMember = getLoginMember(session);

            Long memberNo =
                    loginMember == null
                            ? null
                            : loginMember.getMemberNo();

            String resultId =
                    subscriptionCalculatorService.saveResult(
                            result,
                            memberNo);

            Map<String, Object> response = new HashMap<String, Object>();
            response.put("resultId", resultId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {

            Map<String, Object> errorResponse = new HashMap<String, Object>();
            errorResponse.put(RESPONSE_MESSAGE, e.getMessage());

            return ResponseEntity
                    .badRequest()
                    .body(errorResponse);
        }
    }

    /**
     * [마이페이지 구독 계산 결과 모달 연동 추가]
     * 로그인 회원이 저장한 구독 계산 결과 목록을 최신순으로 반환한다.
     */
    @GetMapping("/saved-results")
    public ResponseEntity<Map<String, Object>> getSavedResults(
            HttpSession session) {

        Long memberNo = LoginMemberUtil.getLoginMemberNo(session);

        if (memberNo == null) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            RESPONSE_SUCCESS, false,
                            RESPONSE_MESSAGE, MESSAGE_LOGIN_REQUIRED));
        }

        List<SubscriptionSavedResultVO> savedResultList =
                subscriptionCalculatorService.getSavedResultsByMember(
                        memberNo);

        return ResponseEntity.ok(Map.of(
                RESPONSE_SUCCESS, true,
                "savedResultList", savedResultList));
    }

    /**
     * [마이페이지 구독 계산 결과 모달 연동 추가]
     * 로그인 회원 본인이 저장한 결과만 개별 삭제한다.
     */
    @DeleteMapping("/saved-results/{resultId}")
    public ResponseEntity<Map<String, Object>> deleteSavedResult(
            @PathVariable("resultId") String resultId,
            HttpSession session) {

        Long memberNo = LoginMemberUtil.getLoginMemberNo(session);

        if (memberNo == null) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            RESPONSE_SUCCESS, false,
                            RESPONSE_MESSAGE, MESSAGE_LOGIN_REQUIRED));
        }

        boolean deleted =
                subscriptionCalculatorService.removeSavedResult(
                        resultId,
                        memberNo);

        if (!deleted) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            RESPONSE_SUCCESS, false,
                            RESPONSE_MESSAGE, "삭제할 저장 결과를 찾을 수 없습니다."));
        }

        return ResponseEntity.ok(Map.of(
                RESPONSE_SUCCESS, true,
                RESPONSE_MESSAGE, "저장 결과를 삭제했습니다."));
    }

    private MemberVO getLoginMember(HttpSession session) {

        Object loginMember = session.getAttribute("loginMember");

        if (loginMember instanceof MemberVO memberVO) {

            return memberVO;
        }

        return null;
    }

    private ContentWishItemVO toWishItem(
            SearchResultVO content) {

        ContentWishItemVO wishItem =
                new ContentWishItemVO();

        wishItem.setTmdbId(content.getTmdbId());
        wishItem.setContentType(content.getContentType());
        wishItem.setTitle(content.getTitle());
        wishItem.setPosterPath(content.getPosterPath());

        List<String> platformNameList =
                new ArrayList<String>();

        // [OTT 구독 조합 계산기 - 담은 작품 OTT 로고 표시 추가]
        // platformNameList와 인덱스를 맞춰 로고 URL도 함께 내려준다.
        List<String> platformLogoList =
                new ArrayList<String>();

        if (content.getPlatformList() != null) {

            for (OttPlatformVO platform : content.getPlatformList()) {

                if (platform != null
                        && platform.getPlatformName() != null) {

                    platformNameList.add(
                            platform.getPlatformName());

                    platformLogoList.add(
                            platform.getLogoImage());
                }
            }
        }

        wishItem.setPlatformNameList(platformNameList);
        wishItem.setPlatformLogoList(platformLogoList);

        return wishItem;
    }
}
