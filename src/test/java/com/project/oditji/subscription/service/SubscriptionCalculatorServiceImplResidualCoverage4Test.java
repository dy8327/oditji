package com.project.oditji.subscription.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.event.dao.OttDiscountDAO;
import com.project.oditji.subscription.dao.SubscriptionDAO;
import com.project.oditji.subscription.vo.ContentWishItemVO;
import com.project.oditji.subscription.vo.PlatformPriceVO;
import com.project.oditji.subscription.vo.SubscriptionCalculationResultVO;

/**
 * SonarQube에서 남은 구독 계산기 보조 메서드의 null/빈 값/매칭/JSON 분기를 보완합니다.
 */
class SubscriptionCalculatorServiceImplResidualCoverage4Test {

    private SubscriptionCalculatorServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SubscriptionCalculatorServiceImpl(
                mock(OttDiscountDAO.class),
                mock(SubscriptionDAO.class));
    }

    @Test
    void copyAndPlatformAssignmentHelpersShouldCoverNullAndMatchingBranches() {
        ContentWishItemVO netflixItem = wishItem("Netflix");
        ContentWishItemVO tvingItem = wishItem("TVING");
        ContentWishItemVO unknownItem = wishItem("Apple TV+");

        List<ContentWishItemVO> source = new ArrayList<ContentWishItemVO>();
        source.add(null);
        source.add(netflixItem);
        source.add(tvingItem);

        @SuppressWarnings("unchecked")
        List<ContentWishItemVO> copied =
                (List<ContentWishItemVO>) ReflectionTestUtils.invokeMethod(
                        service,
                        "copyNonNullWishItems",
                        source);

        assertEquals(List.of(netflixItem, tvingItem), copied);

        PlatformPriceVO netflix = platform("NETFLIX");
        PlatformPriceVO missingCode = platform(null);

        invokeAssign(null, List.of(netflixItem));
        invokeAssign(new ArrayList<PlatformPriceVO>(), List.of(netflixItem));
        invokeAssign(List.of(netflix), null);
        invokeAssign(List.of(netflix), new ArrayList<ContentWishItemVO>());

        List<PlatformPriceVO> selected = new ArrayList<PlatformPriceVO>();
        selected.add(null);
        selected.add(missingCode);
        selected.add(netflix);

        List<ContentWishItemVO> contentList = new ArrayList<ContentWishItemVO>();
        contentList.add(null);
        contentList.add(netflixItem);
        contentList.add(tvingItem);
        contentList.add(unknownItem);

        invokeAssign(selected, contentList);

        assertEquals(List.of(netflixItem), netflix.getContentList());
        assertTrue(missingCode.getContentList().isEmpty());

        assertTrue(invokeAvailability(netflixItem, "NETFLIX"));
        assertFalse(invokeAvailability(tvingItem, "NETFLIX"));
    }

    @Test
    void jsonSerializationHelpersShouldCoverNullAndPresentOptionalValues() {
        PlatformPriceVO noDiscount = platform("NETFLIX");
        noDiscount.setPlatformName("Netflix");
        noDiscount.setRegularPrice(17000);
        noDiscount.setBestPrice(17000);

        PlatformPriceVO discounted = platform("TVING");
        discounted.setPlatformName("TVING");
        discounted.setRegularPrice(14000);
        discounted.setBestPrice(9000);
        discounted.setDiscountSource("CARD");
        discounted.setDiscountTitle("카드 할인");

        ContentWishItemVO allNull = new ContentWishItemVO();
        ContentWishItemVO full = new ContentWishItemVO();
        full.setTmdbId(100L);
        full.setContentType("MOVIE");
        full.setTitle("작품");
        full.setPosterPath("/poster.jpg");
        full.setPlatformNameList(List.of("Netflix", "TVING"));

        ContentWishItemVO noTitle = new ContentWishItemVO();
        ContentWishItemVO unresolved = new ContentWishItemVO();
        unresolved.setTitle("미해결");

        List<ContentWishItemVO> unresolvedList = new ArrayList<ContentWishItemVO>();
        unresolvedList.add(null);
        unresolvedList.add(noTitle);
        unresolvedList.add(unresolved);

        List<ContentWishItemVO> contentList = new ArrayList<ContentWishItemVO>();
        contentList.add(null);
        contentList.add(allNull);
        contentList.add(full);

        SubscriptionCalculationResultVO result = new SubscriptionCalculationResultVO();
        result.setSelectedPlatformList(List.of(noDiscount, discounted));
        result.setUnresolvedItemList(unresolvedList);
        result.setContentList(contentList);
        result.setAllPlatformMonthlyPrice(31000);

        String json = ReflectionTestUtils.invokeMethod(
                service,
                "toSelectedServicesJson",
                result);

        JSONObject root = new JSONObject(json);
        JSONArray platforms = root.getJSONArray("selectedPlatformList");
        JSONArray unresolvedTitles = root.getJSONArray("unresolvedTitleList");
        JSONArray contents = root.getJSONArray("contentList");

        assertEquals(2, platforms.length());
        assertTrue(platforms.getJSONObject(0).isNull("discountSource"));
        assertEquals("CARD", platforms.getJSONObject(1).getString("discountSource"));
        assertEquals(1, unresolvedTitles.length());
        assertEquals("미해결", unresolvedTitles.getString(0));
        assertEquals(2, contents.length());
        assertTrue(contents.getJSONObject(0).isNull("tmdbId"));
        assertEquals(100L, contents.getJSONObject(1).getLong("tmdbId"));
    }

    @Test
    void jsonParsingHelpersShouldCoverNullAndEveryOptionalFieldBranch() {
        assertTrue(invokeParsePlatforms(null).isEmpty());
        assertTrue(invokeParseContents(null).isEmpty());
        assertTrue(invokeParsePlatformNames(null).isEmpty());

        JSONArray platformArray = new JSONArray();
        platformArray.put(new JSONObject()
                .put("platformCode", "NETFLIX")
                .put("platformName", "Netflix")
                .put("regularPrice", JSONObject.NULL)
                .put("bestPrice", 12000)
                .put("discountSource", JSONObject.NULL)
                .put("discountTitle", "프로모션"));
        platformArray.put(new JSONObject()
                .put("platformCode", "TVING")
                .put("platformName", "TVING")
                .put("regularPrice", 14000)
                .put("bestPrice", JSONObject.NULL)
                .put("discountSource", "CARD")
                .put("discountTitle", JSONObject.NULL));

        List<PlatformPriceVO> platforms = invokeParsePlatforms(platformArray);

        assertEquals(2, platforms.size());
        assertNull(platforms.get(0).getRegularPrice());
        assertEquals(Integer.valueOf(12000), platforms.get(0).getBestPrice());
        assertNull(platforms.get(0).getDiscountSource());
        assertEquals("프로모션", platforms.get(0).getDiscountTitle());
        assertEquals(Integer.valueOf(14000), platforms.get(1).getRegularPrice());
        assertNull(platforms.get(1).getBestPrice());
        assertEquals("CARD", platforms.get(1).getDiscountSource());
        assertNull(platforms.get(1).getDiscountTitle());

        JSONArray names = new JSONArray();
        names.put(JSONObject.NULL);
        names.put("   ");
        names.put("Netflix");

        JSONArray contentArray = new JSONArray();
        contentArray.put(new JSONObject()
                .put("tmdbId", JSONObject.NULL)
                .put("contentType", JSONObject.NULL)
                .put("title", JSONObject.NULL)
                .put("posterPath", JSONObject.NULL));
        contentArray.put(new JSONObject()
                .put("tmdbId", 200L)
                .put("contentType", "TV")
                .put("title", "드라마")
                .put("posterPath", "/tv.jpg")
                .put("platformNameList", names));

        List<ContentWishItemVO> contents = invokeParseContents(contentArray);

        assertEquals(2, contents.size());
        assertNull(contents.get(0).getTmdbId());
        assertNull(contents.get(0).getContentType());
        assertNull(contents.get(0).getTitle());
        assertNull(contents.get(0).getPosterPath());
        assertTrue(contents.get(0).getPlatformNameList().isEmpty());

        assertEquals(Long.valueOf(200L), contents.get(1).getTmdbId());
        assertEquals("TV", contents.get(1).getContentType());
        assertEquals("드라마", contents.get(1).getTitle());
        assertEquals("/tv.jpg", contents.get(1).getPosterPath());
        assertEquals(List.of("Netflix"), contents.get(1).getPlatformNameList());
    }

    @Test
    void savedResultExtractorsShouldCoverNullBlankBrokenAndValidJson() {
        assertTrue(invokeExtractContentList(null).isEmpty());
        assertTrue(invokeExtractContentList("   ").isEmpty());
        assertTrue(invokeExtractContentList("{broken").isEmpty());

        assertTrue(invokeExtractPlatformGroupList(null).isEmpty());
        assertTrue(invokeExtractPlatformGroupList("   ").isEmpty());
        assertTrue(invokeExtractPlatformGroupList("{broken").isEmpty());

        String json = "{"
                + "\"selectedPlatformList\":[{"
                + "\"platformCode\":\"NETFLIX\","
                + "\"platformName\":\"Netflix\","
                + "\"regularPrice\":17000,"
                + "\"bestPrice\":12000,"
                + "\"discountSource\":null,"
                + "\"discountTitle\":null}],"
                + "\"contentList\":[{"
                + "\"tmdbId\":1,"
                + "\"contentType\":\"MOVIE\","
                + "\"title\":\"영화\","
                + "\"posterPath\":null,"
                + "\"platformNameList\":[\"Netflix\"]}]}";

        List<ContentWishItemVO> contents = invokeExtractContentList(json);
        List<PlatformPriceVO> groups = invokeExtractPlatformGroupList(json);

        assertEquals(1, contents.size());
        assertEquals("영화", contents.get(0).getTitle());
        assertEquals(1, groups.size());
        assertEquals("NETFLIX", groups.get(0).getPlatformCode());
        assertEquals(List.of("영화"),
                groups.get(0).getContentList().stream()
                        .map(ContentWishItemVO::getTitle)
                        .toList());
    }

    private void invokeAssign(
            List<PlatformPriceVO> selected,
            List<ContentWishItemVO> contents) {

        ReflectionTestUtils.invokeMethod(
                service,
                "assignContentToPlatforms",
                selected,
                contents);
    }

    private boolean invokeAvailability(
            ContentWishItemVO item,
            String platformCode) {

        Boolean result = ReflectionTestUtils.invokeMethod(
                service,
                "isAvailableOnPlatform",
                item,
                platformCode);

        return Boolean.TRUE.equals(result);
    }

    private List<PlatformPriceVO> invokeParsePlatforms(JSONArray array) {
        return ReflectionTestUtils.invokeMethod(
                service,
                "parsePlatformList",
                array);
    }

    private List<ContentWishItemVO> invokeParseContents(JSONArray array) {
        return ReflectionTestUtils.invokeMethod(
                service,
                "parseContentList",
                array);
    }

    private List<String> invokeParsePlatformNames(JSONArray array) {
        return ReflectionTestUtils.invokeMethod(
                service,
                "parsePlatformNameList",
                array);
    }

    private List<ContentWishItemVO> invokeExtractContentList(String json) {
        return ReflectionTestUtils.invokeMethod(
                service,
                "extractContentList",
                json);
    }

    private List<PlatformPriceVO> invokeExtractPlatformGroupList(String json) {
        return ReflectionTestUtils.invokeMethod(
                service,
                "extractPlatformGroupList",
                json);
    }

    private ContentWishItemVO wishItem(String platformName) {
        ContentWishItemVO item = new ContentWishItemVO();
        item.setPlatformNameList(List.of(platformName));
        return item;
    }

    private PlatformPriceVO platform(String code) {
        PlatformPriceVO platform = new PlatformPriceVO();
        platform.setPlatformCode(code);
        return platform;
    }
}
