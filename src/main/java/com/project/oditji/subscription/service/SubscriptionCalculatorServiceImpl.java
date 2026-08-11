package com.project.oditji.subscription.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.project.oditji.event.dao.OttDiscountDAO;
import com.project.oditji.subscription.util.OttPlatformCodeUtil;
import com.project.oditji.subscription.vo.ContentWishItemVO;
import com.project.oditji.subscription.vo.PlatformPriceVO;
import com.project.oditji.subscription.vo.SubscriptionCalculationResultVO;

@Service
public class SubscriptionCalculatorServiceImpl
        implements SubscriptionCalculatorService {

    /*
     * 현재 서비스에 등록된 OTT 플랫폼은 6개(넷플릭스/티빙/웨이브/디즈니+/왓챠/쿠팡플레이)뿐이라
     * 완전탐색(2^N 부분집합)으로도 충분히 빠르다. 향후 플랫폼이 크게 늘어날 경우를 대비해
     * 안전장치로 상한을 둔다.
     */
    private static final int MAX_BRUTE_FORCE_PLATFORM_COUNT = 20;

    private final OttDiscountDAO ottDiscountDAO;

    public SubscriptionCalculatorServiceImpl(
            OttDiscountDAO ottDiscountDAO) {

        this.ottDiscountDAO = ottDiscountDAO;
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

        return result;
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
}
