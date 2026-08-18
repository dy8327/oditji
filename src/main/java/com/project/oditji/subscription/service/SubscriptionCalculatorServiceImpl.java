package com.project.oditji.subscription.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.common.util.DateTimeUtil;
import com.project.oditji.event.dao.OttDiscountDAO;
import com.project.oditji.subscription.dao.SubscriptionDAO;
import com.project.oditji.subscription.util.OttPlatformCodeUtil;
import com.project.oditji.subscription.vo.ContentWishItemVO;
import com.project.oditji.subscription.vo.PlatformPriceVO;
import com.project.oditji.subscription.vo.SubscriptionCalculationResultVO;
import com.project.oditji.subscription.vo.SubscriptionSavedResultVO;
import com.project.oditji.subscription.vo.SubscriptionShareVO;

@Service
public class SubscriptionCalculatorServiceImpl
        implements SubscriptionCalculatorService {

    /*
     * 현재 서비스에 등록된 OTT 플랫폼은 6개(넷플릭스/티빙/웨이브/디즈니+/왓챠/쿠팡플레이)뿐이라
     * 완전탐색(2^N 부분집합)으로도 충분히 빠르다. 향후 플랫폼이 크게 늘어날 경우를 대비해
     * 안전장치로 상한을 둔다.
     */
    private static final int MAX_BRUTE_FORCE_PLATFORM_COUNT = 20;

    /** 공유 링크 resultId 접두사 (SUBS_ + UUID 32자 = 총 37자, VARCHAR2(64) 여유 있음) */
    private static final String RESULT_ID_PREFIX = "SUBS_";

    /*
     * [비회원 공유 링크 임시 보관 추가]
     * 비회원이 만든 공유 링크는 이 기간이 지나면 자동으로 삭제된다.
     * 회원이 로그인 상태로 저장한 결과는 대상이 아니라 영구 보관된다.
     */
    private static final int GUEST_RESULT_EXPIRE_DAYS = 30;

    // [SonarQube] 저장/복원에 공통으로 쓰는 JSON 키를 상수로 관리합니다.
    private static final String JSON_KEY_DISCOUNT_SOURCE = "discountSource";
    private static final String JSON_KEY_DISCOUNT_TITLE = "discountTitle";
    private static final String JSON_KEY_SELECTED_PLATFORM_LIST = "selectedPlatformList";
    private static final String JSON_KEY_PLATFORM_NAME = "platformName";
    private static final String JSON_KEY_CONTENT_LIST = "contentList";
    private static final String JSON_KEY_PLATFORM_NAME_LIST = "platformNameList";
    private static final String JSON_KEY_TMDB_ID = "tmdbId";
    private static final String JSON_KEY_CONTENT_TYPE = "contentType";
    private static final String JSON_KEY_TITLE = "title";
    private static final String JSON_KEY_POSTER_PATH = "posterPath";

    private final OttDiscountDAO ottDiscountDAO;

    private final SubscriptionDAO subscriptionDAO;

    public SubscriptionCalculatorServiceImpl(
            OttDiscountDAO ottDiscountDAO,
            SubscriptionDAO subscriptionDAO) {

        this.ottDiscountDAO = ottDiscountDAO;
        this.subscriptionDAO = subscriptionDAO;
    }

    @Override
    public SubscriptionCalculationResultVO calculate(
            List<ContentWishItemVO> wishItemList,
            String telecomCode,
            String cardCompany,
            String membershipName) {

        SubscriptionCalculationResultVO result =
                new SubscriptionCalculationResultVO();

        if (wishItemList == null
                || wishItemList.isEmpty()) {

            return result;
        }

        /*
         * 계산 성공 여부와 무관하게, 이번 계산에 사용된 전체 콘텐츠 목록을
         * 그대로 결과에 담아둔다. 마이페이지 모달과 결과 공유 화면에서
         * "어떤 콘텐츠를 골라 계산했는지" 보여줄 때 사용한다.
         */
        // [SonarQube] null 제거 반복문을 분리해 계산 메서드의 인지 복잡도를 낮춥니다.
        List<ContentWishItemVO> contentList =
                copyNonNullWishItems(wishItemList);

        result.setContentList(contentList);

        Map<String, PlatformPriceVO> priceByCode =
                loadPriceByCode(
                        blankToNull(telecomCode),
                        blankToNull(cardCompany),
                        blankToNull(membershipName));

        /*
         * 각 콘텐츠를 볼 수 있는 플랫폼 중, 가격 정보가 있는 코드만 남긴다.
         * 하나도 남지 않으면(가격 정보 없는 플랫폼에서만 볼 수 있으면) 계산에서 제외한다.
         */
        List<Set<String>> requirementList =
                new ArrayList<Set<String>>();

        Set<String> universe =
                new LinkedHashSet<String>();

        for (ContentWishItemVO item : wishItemList) {

            if (item != null) {

                Set<String> coverableCodes =
                        resolveCoverableCodes(
                                item,
                                priceByCode);

                if (coverableCodes.isEmpty()) {

                    result.getUnresolvedItemList()
                            .add(item);
                } else {

                    requirementList.add(coverableCodes);
                    universe.addAll(coverableCodes);
                }
            }
        }

        if (requirementList.isEmpty()) {

            return result;
        }

        int allPlatformTotal = 0;

        for (String code : universe) {

            allPlatformTotal +=
                    priceByCode.get(code)
                            .getBestPrice();
        }

        result.setAllPlatformMonthlyPrice(
                allPlatformTotal);

        Set<String> bestCombination =
                findCheapestCombination(
                        universe,
                        requirementList,
                        priceByCode);

        int totalPrice = 0;
        int totalRegularPrice = 0;

        List<PlatformPriceVO> selected =
                new ArrayList<PlatformPriceVO>();

        for (String code : bestCombination) {

            PlatformPriceVO priceVO =
                    priceByCode.get(code);

            selected.add(priceVO);
            totalPrice += priceVO.getBestPrice();

            if (priceVO.getRegularPrice() != null) {

                totalRegularPrice += priceVO.getRegularPrice();
            } else {

                totalRegularPrice += priceVO.getBestPrice();
            }
        }

        result.setSelectedPlatformList(selected);
        result.setTotalMonthlyPrice(totalPrice);
        result.setTotalRegularMonthlyPrice(totalRegularPrice);

        assignContentToPlatforms(selected, contentList);

        return result;
    }

    /** 계산 대상 위시리스트에서 null 항목만 제외해 원래 순서를 유지한 복사본을 만든다. */
    private List<ContentWishItemVO> copyNonNullWishItems(
            List<ContentWishItemVO> wishItemList) {

        List<ContentWishItemVO> contentList =
                new ArrayList<ContentWishItemVO>();

        for (ContentWishItemVO item : wishItemList) {

            if (item != null) {

                contentList.add(item);
            }
        }

        return contentList;
    }

    /**
     * 선택된 플랫폼별로, 그 플랫폼에서 볼 수 있는 콘텐츠 목록을 채워 넣는다.
     * 한 콘텐츠가 선택된 플랫폼 여러 곳에서 모두 볼 수 있으면 해당하는 모든 플랫폼에 표시한다.
     */
    private void assignContentToPlatforms(
            List<PlatformPriceVO> selectedPlatformList,
            List<ContentWishItemVO> contentList) {

        if (selectedPlatformList == null
                || selectedPlatformList.isEmpty()
                || contentList == null
                || contentList.isEmpty()) {

            return;
        }

        for (PlatformPriceVO platform : selectedPlatformList) {

            if (platform == null
                    || platform.getPlatformCode() == null) {

                continue;
            }

            // [SonarQube] 중첩된 콘텐츠/플랫폼 탐색을 보조 메서드로 분리합니다.
            platform.setContentList(
                    findMatchedContentList(
                            platform.getPlatformCode(),
                            contentList));
        }
    }

    /** 특정 플랫폼에서 시청 가능한 콘텐츠만 원래 순서대로 반환한다. */
    private List<ContentWishItemVO> findMatchedContentList(
            String platformCode,
            List<ContentWishItemVO> contentList) {

        List<ContentWishItemVO> matchedList =
                new ArrayList<ContentWishItemVO>();

        for (ContentWishItemVO item : contentList) {

            if (item != null
                    && isAvailableOnPlatform(item, platformCode)) {

                matchedList.add(item);
            }
        }

        return matchedList;
    }

    /** 콘텐츠 제공 플랫폼명 목록에 지정 플랫폼 코드가 포함되는지 확인한다. */
    private boolean isAvailableOnPlatform(
            ContentWishItemVO item,
            String platformCode) {

        for (String platformName : item.getPlatformNameList()) {

            String code =
                    OttPlatformCodeUtil
                            .fromContentPlatformName(
                                    platformName);

            if (platformCode.equals(code)) {

                return true;
            }
        }

        return false;
    }

    /** 화면에서 넘어온 빈 문자열/공백을 필터 미선택(null)으로 취급한다. */
    private String blankToNull(String value) {

        if (value == null || value.trim().isEmpty()) {

            return null;
        }

        return value.trim();
    }

    private Map<String, PlatformPriceVO> loadPriceByCode(
            String telecomCode,
            String cardCompany,
            String membershipName) {

        List<PlatformPriceVO> priceList =
                ottDiscountDAO.selectBestPriceByPlatform(
                        telecomCode,
                        cardCompany,
                        membershipName);

        Map<String, PlatformPriceVO> priceByCode =
                new LinkedHashMap<String, PlatformPriceVO>();

        if (priceList == null) {

            return priceByCode;
        }

        for (PlatformPriceVO priceVO : priceList) {

            if (priceVO == null
                    || priceVO.getPlatformCode() == null
                    || priceVO.getBestPrice() == null) {

                continue;
            }

            priceByCode.put(
                    priceVO.getPlatformCode(),
                    priceVO);
        }

        return priceByCode;
    }

    private Set<String> resolveCoverableCodes(
            ContentWishItemVO item,
            Map<String, PlatformPriceVO> priceByCode) {

        Set<String> coverableCodes =
                new LinkedHashSet<String>();

        for (String platformName : item.getPlatformNameList()) {

            String code =
                    OttPlatformCodeUtil
                            .fromContentPlatformName(
                                    platformName);

            if (code != null
                    && priceByCode.containsKey(code)) {

                coverableCodes.add(code);
            }
        }

        return coverableCodes;
    }

    /**
     * universe의 모든 부분집합 중, requirementList의 모든 항목을 최소 1개 이상의
     * 플랫폼으로 커버하면서 총 가격이 가장 낮은(동률이면 플랫폼 수가 더 적은) 조합을 찾는다.
     */
    private Set<String> findCheapestCombination(
            Set<String> universe,
            List<Set<String>> requirementList,
            Map<String, PlatformPriceVO> priceByCode) {

        List<String> codeList =
                new ArrayList<String>(universe);

        int platformCount = codeList.size();

        if (platformCount == 0
                || platformCount > MAX_BRUTE_FORCE_PLATFORM_COUNT) {

            /*
             * 이론상 도달하지 않는 방어 분기다(현재 플랫폼 6개 고정).
             * 플랫폼이 상한을 넘는 예외 상황에서는 완전탐색 대신
             * 필요한 플랫폼을 전부 선택해 안전하게 폴백한다.
             */
            return universe;
        }

        Set<String> bestCombination = null;
        int bestPrice = Integer.MAX_VALUE;
        int bestSize = Integer.MAX_VALUE;

        int subsetCount = 1 << platformCount;

        for (int mask = 1; mask < subsetCount; mask++) {

            Set<String> candidate =
                    new LinkedHashSet<String>();

            int price = 0;

            for (int bit = 0; bit < platformCount; bit++) {

                if ((mask & (1 << bit)) != 0) {

                    String code = codeList.get(bit);
                    candidate.add(code);
                    price += priceByCode.get(code)
                            .getBestPrice();
                }
            }

            if (!coversAllRequirements(
                    candidate,
                    requirementList)) {

                continue;
            }

            int size = candidate.size();

            if (price < bestPrice
                    || (price == bestPrice
                            && size < bestSize)) {

                bestCombination = candidate;
                bestPrice = price;
                bestSize = size;
            }
        }

        return bestCombination == null
                ? universe
                : bestCombination;
    }

    private boolean coversAllRequirements(
            Set<String> candidate,
            List<Set<String>> requirementList) {

        for (Set<String> requirement : requirementList) {

            boolean covered = false;

            for (String code : requirement) {

                if (candidate.contains(code)) {

                    covered = true;
                    break;
                }
            }

            if (!covered) {

                return false;
            }
        }

        return true;
    }

    @Override
    public String saveResult(
            SubscriptionCalculationResultVO result,
            Long memberNo) {

        if (result == null
                || result.getSelectedPlatformList().isEmpty()) {

            throw new IllegalArgumentException(
                    "저장할 계산 결과가 없습니다.");
        }

        String resultId = createResultId();

        SubscriptionShareVO shareVO = new SubscriptionShareVO();

        shareVO.setResultId(resultId);
        shareVO.setMemberNo(memberNo);
        shareVO.setTotalPrice(
                result.getTotalRegularMonthlyPrice());
        shareVO.setDiscountPrice(
                result.getTotalRegularMonthlyPrice()
                        - result.getTotalMonthlyPrice());
        shareVO.setFinalPrice(
                result.getTotalMonthlyPrice());
        shareVO.setSelectedServicesJson(
                toSelectedServicesJson(result));

        /*
         * [비회원 공유 링크 임시 보관 추가]
         * memberNo가 없는(비로그인) 저장만 만료 시각을 채운다.
         * 회원 저장은 EXPIRES_AT을 NULL로 두어 계속 보관한다.
         */
        shareVO.setExpiresAt(
                memberNo == null
                        ? LocalDateTime.now(DateTimeUtil.KOREA_ZONE)
                                .plusDays(GUEST_RESULT_EXPIRE_DAYS)
                        : null);

        subscriptionDAO.insertResult(shareVO);

        return resultId;
    }

    @Override
    public int deleteExpiredResults() {
        return subscriptionDAO.deleteExpiredResults();
    }

    @Override
    public int getSavedResultCount(Long memberNo) {

        if (memberNo == null) {

            return 0;
        }

        return subscriptionDAO
                .selectResultsByMember(memberNo)
                .size();
    }

    @Override
    public List<SubscriptionSavedResultVO> getSavedResultsByMember(
            Long memberNo) {

        List<SubscriptionSavedResultVO> savedResultList =
                new ArrayList<SubscriptionSavedResultVO>();

        if (memberNo == null) {

            return savedResultList;
        }

        List<SubscriptionShareVO> shareVOList =
                subscriptionDAO.selectResultsByMember(memberNo);

        if (shareVOList == null) {

            return savedResultList;
        }

        for (SubscriptionShareVO shareVO : shareVOList) {

            savedResultList.add(
                    toSavedResultVO(shareVO));
        }

        return savedResultList;
    }

    @Override
    @Transactional
    public boolean removeSavedResult(
            String resultId,
            Long memberNo) {

        if (resultId == null
                || resultId.trim().isEmpty()
                || memberNo == null) {

            return false;
        }

        return subscriptionDAO.deleteResultByIdAndMember(
                resultId.trim(),
                memberNo) > 0;
    }

    /**
     * 마이페이지 모달 목록에 필요한 값만 담아 SubscriptionSavedResultVO로 변환한다.
     * SELECTED_SERVICES JSON에서는 플랫폼 이름 목록만 뽑아낸다.
     */
    private SubscriptionSavedResultVO toSavedResultVO(
            SubscriptionShareVO shareVO) {

        SubscriptionSavedResultVO savedResult =
                new SubscriptionSavedResultVO();

        savedResult.setResultId(shareVO.getResultId());
        savedResult.setCreatedAt(shareVO.getCreatedAt());
        savedResult.setTotalPrice(shareVO.getTotalPrice());
        savedResult.setDiscountPrice(shareVO.getDiscountPrice());
        savedResult.setFinalPrice(shareVO.getFinalPrice());
        savedResult.setPlatformNameList(
                extractPlatformNameList(
                        shareVO.getSelectedServicesJson()));
        savedResult.setContentList(
                extractContentList(
                        shareVO.getSelectedServicesJson()));
        savedResult.setPlatformGroupList(
                extractPlatformGroupList(
                        shareVO.getSelectedServicesJson()));

        return savedResult;
    }

    /**
     * 저장된 SELECTED_SERVICES JSON에서 선택 플랫폼별로 그 플랫폼의 콘텐츠를 묶은 목록을 뽑아낸다.
     * JSON이 손상됐거나 없으면 빈 목록을 반환한다.
     */
    private List<PlatformPriceVO> extractPlatformGroupList(
            String selectedServicesJson) {

        if (selectedServicesJson == null
                || selectedServicesJson.trim().isEmpty()) {

            return new ArrayList<PlatformPriceVO>();
        }

        try {

            JSONObject root = new JSONObject(selectedServicesJson);

            List<PlatformPriceVO> platformGroupList =
                    parsePlatformList(
                            root.optJSONArray(JSON_KEY_SELECTED_PLATFORM_LIST));

            List<ContentWishItemVO> contentList =
                    parseContentList(
                            root.optJSONArray(JSON_KEY_CONTENT_LIST));

            assignContentToPlatforms(
                    platformGroupList,
                    contentList);

            return platformGroupList;

        } catch (JSONException e) {

            /* 저장된 JSON이 손상된 경우 빈 목록으로 방어적으로 대응한다. */
            return new ArrayList<PlatformPriceVO>();
        }
    }

    /**
     * 저장된 SELECTED_SERVICES JSON에서 선택 플랫폼 이름 목록만 방어적으로 뽑아낸다.
     * JSON이 손상됐거나 없으면 빈 목록을 반환한다.
     */
    private List<String> extractPlatformNameList(
            String selectedServicesJson) {

        List<String> platformNameList =
                new ArrayList<String>();

        if (selectedServicesJson == null
                || selectedServicesJson.trim().isEmpty()) {

            return platformNameList;
        }

        try {

            JSONObject root = new JSONObject(selectedServicesJson);

            JSONArray platformArray =
                    root.optJSONArray(JSON_KEY_SELECTED_PLATFORM_LIST);

            if (platformArray != null) {

                for (int i = 0; i < platformArray.length(); i++) {

                    JSONObject platformJson =
                            platformArray.getJSONObject(i);

                    String platformName =
                            platformJson.optString(JSON_KEY_PLATFORM_NAME, null);

                    if (platformName != null
                            && !platformName.trim().isEmpty()) {

                        platformNameList.add(platformName);
                    }
                }
            }

        } catch (JSONException e) {

            /* 저장된 JSON이 손상된 경우 빈 목록으로 방어적으로 대응한다. */
            return new ArrayList<String>();
        }

        return platformNameList;
    }

    /**
     * 저장된 SELECTED_SERVICES JSON에서 계산에 사용된 콘텐츠(담은 작품) 목록을
     * 방어적으로 뽑아낸다. JSON이 손상됐거나 없으면 빈 목록을 반환한다.
     */
    private List<ContentWishItemVO> extractContentList(
            String selectedServicesJson) {

        if (selectedServicesJson == null
                || selectedServicesJson.trim().isEmpty()) {

            return new ArrayList<ContentWishItemVO>();
        }

        try {

            JSONObject root = new JSONObject(selectedServicesJson);

            return parseContentList(
                    root.optJSONArray(JSON_KEY_CONTENT_LIST));

        } catch (JSONException e) {

            /* 저장된 JSON이 손상된 경우 빈 목록으로 방어적으로 대응한다. */
            return new ArrayList<ContentWishItemVO>();
        }
    }

    /** contentList JSON 배열을 ContentWishItemVO 목록으로 변환한다. */
    private List<ContentWishItemVO> parseContentList(
            JSONArray contentArray) {

        List<ContentWishItemVO> contentList =
                new ArrayList<ContentWishItemVO>();

        if (contentArray == null) {

            return contentList;
        }

        for (int i = 0; i < contentArray.length(); i++) {

            JSONObject contentJson = contentArray.getJSONObject(i);

            ContentWishItemVO item = new ContentWishItemVO();

            item.setTmdbId(
                    contentJson.isNull(JSON_KEY_TMDB_ID)
                            ? null
                            : contentJson.optLong(JSON_KEY_TMDB_ID));
            item.setContentType(
                    contentJson.isNull(JSON_KEY_CONTENT_TYPE)
                            ? null
                            : contentJson.optString(JSON_KEY_CONTENT_TYPE, null));
            item.setTitle(
                    contentJson.isNull(JSON_KEY_TITLE)
                            ? null
                            : contentJson.optString(JSON_KEY_TITLE, null));
            item.setPosterPath(
                    contentJson.isNull(JSON_KEY_POSTER_PATH)
                            ? null
                            : contentJson.optString(JSON_KEY_POSTER_PATH, null));
            item.setPlatformNameList(
                    parsePlatformNameList(
                            contentJson.optJSONArray(JSON_KEY_PLATFORM_NAME_LIST)));

            contentList.add(item);
        }

        return contentList;
    }

    /** contentList JSON 항목의 platformNameList 배열을 List<String>으로 변환한다. */
    private List<String> parsePlatformNameList(
            JSONArray platformNameArray) {

        List<String> platformNameList =
                new ArrayList<String>();

        if (platformNameArray == null) {

            return platformNameList;
        }

        for (int i = 0; i < platformNameArray.length(); i++) {

            String platformName =
                    platformNameArray.optString(i, null);

            if (platformName != null
                    && !platformName.trim().isEmpty()) {

                platformNameList.add(platformName);
            }
        }

        return platformNameList;
    }

    @Override
    public SubscriptionCalculationResultVO restoreResult(
            String resultId) {

        if (resultId == null
                || resultId.trim().isEmpty()) {

            return null;
        }

        SubscriptionShareVO shareVO =
                subscriptionDAO.selectResultById(
                        resultId.trim());

        if (shareVO == null) {

            return null;
        }

        return fromSelectedServicesJson(shareVO);
    }

    /** 공유 링크에 쓸 고유 resultId를 UUID 기반으로 발급한다. */
    private String createResultId() {

        String uuid = UUID.randomUUID()
                .toString()
                .replace("-", "");

        return RESULT_ID_PREFIX + uuid;
    }

    /**
     * 계산 결과 중 저장이 필요한 부분(선택 플랫폼 상세, 미해결 작품 제목,
     * 전체 개별구독 합계)만 JSON 문자열로 직렬화한다. 프로젝트 컨벤션에 맞춰
     * 별도 ObjectMapper 없이 org.json으로 직접 구성한다.
     */
    private String toSelectedServicesJson(
            SubscriptionCalculationResultVO result) {

        JSONObject root = new JSONObject();

        // [SonarQube] 각 JSON 배열 직렬화를 분리해 이 메서드의 인지 복잡도를 낮춥니다.
        root.put(
                JSON_KEY_SELECTED_PLATFORM_LIST,
                toPlatformJsonArray(result.getSelectedPlatformList()));
        root.put(
                "unresolvedTitleList",
                toUnresolvedTitleJsonArray(result.getUnresolvedItemList()));
        root.put(
                "allPlatformMonthlyPrice",
                result.getAllPlatformMonthlyPrice());
        root.put(
                JSON_KEY_CONTENT_LIST,
                toContentJsonArray(result.getContentList()));

        return root.toString();
    }

    /** 선택 플랫폼 목록을 저장용 JSON 배열로 변환한다. */
    private JSONArray toPlatformJsonArray(
            List<PlatformPriceVO> selectedPlatformList) {

        JSONArray platformArray = new JSONArray();

        for (PlatformPriceVO platform : selectedPlatformList) {

            platformArray.put(toPlatformJson(platform));
        }

        return platformArray;
    }

    /** 플랫폼 가격/할인 정보를 저장용 JSON 객체로 변환한다. */
    private JSONObject toPlatformJson(PlatformPriceVO platform) {

        JSONObject platformJson = new JSONObject();

        platformJson.put("platformCode", platform.getPlatformCode());
        platformJson.put(JSON_KEY_PLATFORM_NAME, platform.getPlatformName());
        platformJson.put("regularPrice", platform.getRegularPrice());
        platformJson.put("bestPrice", platform.getBestPrice());
        platformJson.put(
                JSON_KEY_DISCOUNT_SOURCE,
                platform.getDiscountSource() == null
                        ? JSONObject.NULL
                        : platform.getDiscountSource());
        platformJson.put(
                JSON_KEY_DISCOUNT_TITLE,
                platform.getDiscountTitle() == null
                        ? JSONObject.NULL
                        : platform.getDiscountTitle());

        return platformJson;
    }

    /** 미해결 콘텐츠 제목 목록을 저장용 JSON 배열로 변환한다. */
    private JSONArray toUnresolvedTitleJsonArray(
            List<ContentWishItemVO> unresolvedItemList) {

        JSONArray unresolvedTitleArray = new JSONArray();

        for (ContentWishItemVO item : unresolvedItemList) {

            if (item != null
                    && item.getTitle() != null) {

                unresolvedTitleArray.put(item.getTitle());
            }
        }

        return unresolvedTitleArray;
    }

    /** 계산 대상 콘텐츠 목록을 저장용 JSON 배열로 변환한다. */
    private JSONArray toContentJsonArray(
            List<ContentWishItemVO> contentList) {

        JSONArray contentArray = new JSONArray();

        for (ContentWishItemVO item : contentList) {

            if (item != null) {

                contentArray.put(toContentJson(item));
            }
        }

        return contentArray;
    }

    /** 콘텐츠 식별/표시 정보를 저장용 JSON 객체로 변환한다. */
    private JSONObject toContentJson(ContentWishItemVO item) {

        JSONObject contentJson = new JSONObject();

        contentJson.put(
                JSON_KEY_TMDB_ID,
                item.getTmdbId() == null
                        ? JSONObject.NULL
                        : item.getTmdbId());
        contentJson.put(
                JSON_KEY_CONTENT_TYPE,
                item.getContentType() == null
                        ? JSONObject.NULL
                        : item.getContentType());
        contentJson.put(
                JSON_KEY_TITLE,
                item.getTitle() == null
                        ? JSONObject.NULL
                        : item.getTitle());
        contentJson.put(
                JSON_KEY_POSTER_PATH,
                item.getPosterPath() == null
                        ? JSONObject.NULL
                        : item.getPosterPath());
        contentJson.put(
                JSON_KEY_PLATFORM_NAME_LIST,
                new JSONArray(item.getPlatformNameList()));

        return contentJson;
    }

    /**
     * 저장된 SELECTED_SERVICES JSON을 SubscriptionCalculationResultVO로 복원한다.
     * JSON이 손상되어 있으면 정가/최종가만 반영된 빈 결과를 방어적으로 반환한다.
     */
    private SubscriptionCalculationResultVO fromSelectedServicesJson(
            SubscriptionShareVO shareVO) {

        SubscriptionCalculationResultVO result =
                new SubscriptionCalculationResultVO();

        result.setTotalRegularMonthlyPrice(shareVO.getTotalPrice());
        result.setTotalMonthlyPrice(shareVO.getFinalPrice());

        try {

            JSONObject root = new JSONObject(
                    shareVO.getSelectedServicesJson());

            List<PlatformPriceVO> selectedList =
                    parsePlatformList(
                            root.optJSONArray(JSON_KEY_SELECTED_PLATFORM_LIST));

            result.setSelectedPlatformList(selectedList);

            JSONArray unresolvedTitleArray =
                    root.optJSONArray("unresolvedTitleList");

            List<ContentWishItemVO> unresolvedList =
                    new ArrayList<ContentWishItemVO>();

            if (unresolvedTitleArray != null) {

                for (int i = 0; i < unresolvedTitleArray.length(); i++) {

                    ContentWishItemVO item = new ContentWishItemVO();
                    item.setTitle(unresolvedTitleArray.getString(i));

                    unresolvedList.add(item);
                }
            }

            result.setUnresolvedItemList(unresolvedList);
            result.setAllPlatformMonthlyPrice(
                    root.optInt("allPlatformMonthlyPrice", 0));

            result.setContentList(
                    parseContentList(
                            root.optJSONArray(JSON_KEY_CONTENT_LIST)));

            assignContentToPlatforms(
                    result.getSelectedPlatformList(),
                    result.getContentList());

        } catch (JSONException e) {

            /* 저장된 JSON이 손상된 경우 정가/최종가만 담긴 빈 결과로 방어적으로 대응한다. */
            return result;
        }

        return result;
    }

    /** 저장된 selectedPlatformList JSON 배열을 PlatformPriceVO 목록으로 변환한다. */
    private List<PlatformPriceVO> parsePlatformList(
            JSONArray platformArray) {

        List<PlatformPriceVO> platformList =
                new ArrayList<PlatformPriceVO>();

        if (platformArray == null) {

            return platformList;
        }

        for (int i = 0; i < platformArray.length(); i++) {

            JSONObject platformJson =
                    platformArray.getJSONObject(i);

            PlatformPriceVO platform = new PlatformPriceVO();

            platform.setPlatformCode(
                    platformJson.optString("platformCode", null));
            platform.setPlatformName(
                    platformJson.optString(JSON_KEY_PLATFORM_NAME, null));
            platform.setRegularPrice(
                    optInteger(platformJson, "regularPrice"));
            platform.setBestPrice(
                    optInteger(platformJson, "bestPrice"));
            platform.setDiscountSource(
                    platformJson.isNull(JSON_KEY_DISCOUNT_SOURCE)
                            ? null
                            : platformJson.optString(JSON_KEY_DISCOUNT_SOURCE, null));
            platform.setDiscountTitle(
                    platformJson.isNull(JSON_KEY_DISCOUNT_TITLE)
                            ? null
                            : platformJson.optString(JSON_KEY_DISCOUNT_TITLE, null));

            platformList.add(platform);
        }

        return platformList;
    }

    private Integer optInteger(JSONObject json, String key) {

        if (json.isNull(key)) {

            return null;
        }

        return json.optInt(key);
    }
}
