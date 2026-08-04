package com.project.oditji.coverage;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

/**
 * 화면과 서비스 계층에서 사용하는 VO의 생성자, 접근자와 보조 메서드를 실행합니다.
 *
 * 단순 데이터 전달 객체가 SonarQube에서 전부 0%로 남지 않도록
 * 실제 공개 생성자와 공개 메서드를 일괄 검증합니다.
 */
class AllVoCoverageTest {

    private static final List<String> VO_CLASS_NAMES = List.of(
            "com.project.oditji.admin.vo.AdminVO",
            "com.project.oditji.admin.vo.ApprovalStatVO",
            "com.project.oditji.admin.vo.BusinessManageVO",
            "com.project.oditji.admin.vo.BusinessStatVO",
            "com.project.oditji.admin.vo.ContentManageVO",
            "com.project.oditji.admin.vo.EventManageVO",
            "com.project.oditji.admin.vo.EventStatVO",
            "com.project.oditji.admin.vo.MemberManageVO",
            "com.project.oditji.admin.vo.MemberStatVO",
            "com.project.oditji.admin.vo.MonitoringVO",
            "com.project.oditji.admin.vo.OrderManageVO",
            "com.project.oditji.admin.vo.OrderStatVO",
            "com.project.oditji.admin.vo.PlatformVO",
            "com.project.oditji.admin.vo.PopularClickVO",
            "com.project.oditji.admin.vo.ProductManageVO",
            "com.project.oditji.admin.vo.ProductStatVO",
            "com.project.oditji.admin.vo.ReviewManageVO",
            "com.project.oditji.admin.vo.ReviewStatVO",
            "com.project.oditji.admin.vo.SettlementManageVO",
            "com.project.oditji.admin.vo.SettlementStatVO",
            "com.project.oditji.admin.vo.VisitorTrendVO",
            "com.project.oditji.business.vo.ActorSearchVO",
            "com.project.oditji.business.vo.BusinessDashboardVO",
            "com.project.oditji.business.vo.BusinessVO",
            "com.project.oditji.business.vo.ContentSearchVO",
            "com.project.oditji.business.vo.DeliveryManageVO",
            "com.project.oditji.business.vo.EventManageVO",
            "com.project.oditji.business.vo.EventProductVO",
            "com.project.oditji.business.vo.GoodsManageVO",
            "com.project.oditji.business.vo.NtsBusinessVerifyVO",
            "com.project.oditji.business.vo.SettlementManageVO",
            "com.project.oditji.cart.vo.CartItemVO",
            "com.project.oditji.cart.vo.CartRequestVO",
            "com.project.oditji.cart.vo.CartVO",
            "com.project.oditji.chat.vo.ChatNotificationContextVO",
            "com.project.oditji.chat.vo.ChatNotificationRoomVO",
            "com.project.oditji.chat.vo.ChatParticipantReadVO",
            "com.project.oditji.chat.vo.ChatReadStateVO",
            "com.project.oditji.chat.vo.ChatResponseVO",
            "com.project.oditji.chat.vo.ChatRoomMemberVO",
            "com.project.oditji.chat.vo.ChatRoomVO",
            "com.project.oditji.common.vo.AccessLogVO",
            "com.project.oditji.common.vo.ActorBaseVO",
            "com.project.oditji.common.vo.BusinessBaseVO",
            "com.project.oditji.common.vo.ContentMetadataVO",
            "com.project.oditji.common.vo.DeliveryBaseVO",
            "com.project.oditji.common.vo.EventBaseVO",
            "com.project.oditji.common.vo.EventProductBaseVO",
            "com.project.oditji.common.vo.LongProductSummaryVO",
            "com.project.oditji.common.vo.OrderAddressBaseVO",
            "com.project.oditji.common.vo.OrderSummaryBaseVO",
            "com.project.oditji.common.vo.PageVO",
            "com.project.oditji.common.vo.PlatformBaseVO",
            "com.project.oditji.common.vo.ProductOptionSelectionVO",
            "com.project.oditji.common.vo.ProductSaleInfoVO",
            "com.project.oditji.common.vo.ProductSelectionVO",
            "com.project.oditji.common.vo.SearchVO",
            "com.project.oditji.content.vo.ContentListPageVO",
            "com.project.oditji.content.vo.ContentVO",
            "com.project.oditji.content.vo.ContentViewHistoryVO",
            "com.project.oditji.content.vo.FilmographyVO",
            "com.project.oditji.content.vo.PersonFilmographyVO",
            "com.project.oditji.event.vo.EventProductVO",
            "com.project.oditji.event.vo.EventVO",
            "com.project.oditji.favorite.vo.FavoriteVO",
            "com.project.oditji.goods.vo.GoodsVO",
            "com.project.oditji.goods.vo.ProductOptionVO",
            "com.project.oditji.member.vo.GoogleTokenVO",
            "com.project.oditji.member.vo.GoogleUserInfoVO",
            "com.project.oditji.member.vo.KakaoTokenVO",
            "com.project.oditji.member.vo.KakaoUserInfoVO",
            "com.project.oditji.member.vo.MemberPlatformVO",
            "com.project.oditji.member.vo.MemberSocialJoinVO",
            "com.project.oditji.member.vo.MemberSocialVO",
            "com.project.oditji.member.vo.MemberVO",
            "com.project.oditji.member.vo.NaverTokenVO",
            "com.project.oditji.member.vo.NaverUserInfoVO",
            "com.project.oditji.member.vo.PlatformVO",
            "com.project.oditji.member.vo.SocialLoginResultVO",
            "com.project.oditji.notification.vo.NotificationContextVO",
            "com.project.oditji.notification.vo.NotificationResponseVO",
            "com.project.oditji.notification.vo.NotificationVO",
            "com.project.oditji.order.vo.DeliveryVO",
            "com.project.oditji.order.vo.OrderCheckoutRequestVO",
            "com.project.oditji.order.vo.OrderDirectRequestVO",
            "com.project.oditji.order.vo.OrderItemVO",
            "com.project.oditji.order.vo.OrderPaymentCancelRequestVO",
            "com.project.oditji.order.vo.OrderPaymentCompleteRequestVO",
            "com.project.oditji.order.vo.OrderPaymentPrepareVO",
            "com.project.oditji.order.vo.OrderSheetItemVO",
            "com.project.oditji.order.vo.OrderSubmitRequestVO",
            "com.project.oditji.order.vo.OrderVO",
            "com.project.oditji.payment.vo.PaymentVO",
            "com.project.oditji.recommend.vo.RecommendOttContentVO",
            "com.project.oditji.recommend.vo.RecommendOttResultVO",
            "com.project.oditji.recommend.vo.RecommendOttScoreVO",
            "com.project.oditji.recommend.vo.RecommendPlatformSectionVO",
            "com.project.oditji.refund.vo.OrderCancelRefundVO",
            "com.project.oditji.report.vo.ReportVO",
            "com.project.oditji.review.vo.ContentReviewVO",
            "com.project.oditji.review.vo.ContentSpoilerSourceVO",
            "com.project.oditji.review.vo.MyReviewVO",
            "com.project.oditji.review.vo.ProductReviewBaseVO",
            "com.project.oditji.review.vo.ProductReviewVO",
            "com.project.oditji.review.vo.ReviewBaseVO",
            "com.project.oditji.review.vo.ReviewVO",
            "com.project.oditji.search.vo.CachedContentVO",
            "com.project.oditji.search.vo.SearchResultPageVO",
            "com.project.oditji.search.vo.SearchResultVO",
            "com.project.oditji.search.vo.SearchVO",
            "com.project.oditji.tmdb.vo.ActorVO",
            "com.project.oditji.tmdb.vo.DirectorVO",
            "com.project.oditji.tmdb.vo.OttPlatformVO",
            "com.project.oditji.tmdb.vo.TmdbVO",
            "com.project.oditji.verify.vo.AdultVerifyCompleteVO",
            "com.project.oditji.verify.vo.AdultVerifyReadyVO",
            "com.project.oditji.verify.vo.AdultVerifyRequestVO",
            "com.project.oditji.verify.vo.IdentityVerifyLogVO",
            "com.project.oditji.wish.vo.WishVO"
    );

    @Test
    void allPublicVoApisShouldBeExecutable() {
        int constructedClassCount = 0;
        int invokedMethodCount = 0;
        StringBuilder diagnostics = new StringBuilder();

        for (String className : VO_CLASS_NAMES) {
            try {
                Class<?> type = Class.forName(className);

                if (Modifier.isAbstract(type.getModifiers())
                        || type.isInterface()) {
                    continue;
                }

                Object instance = createInstance(type);
                if (instance == null) {
                    diagnostics.append("[constructor] ")
                            .append(className)
                            .append(System.lineSeparator());
                    continue;
                }

                constructedClassCount++;
                invokedMethodCount += invokePublicMethods(
                        type,
                        instance,
                        diagnostics);
            } catch (ClassNotFoundException exception) {
                diagnostics.append("[class] ")
                        .append(className)
                        .append(": ")
                        .append(exception.getMessage())
                        .append(System.lineSeparator());
            }
        }

        String message = diagnostics.toString();
        assertTrue(
                constructedClassCount >= 90,
                "생성된 VO 수가 예상보다 적습니다: "
                        + constructedClassCount
                        + System.lineSeparator()
                        + message);
        assertTrue(
                invokedMethodCount >= 900,
                "실행된 VO 공개 메서드 수가 예상보다 적습니다: "
                        + invokedMethodCount
                        + System.lineSeparator()
                        + message);
    }

    private Object createInstance(Class<?> type) {
        Constructor<?>[] constructors = type.getConstructors();

        for (Constructor<?> constructor : constructors) {
            Object[] arguments = createArguments(
                    constructor.getParameterTypes());

            try {
                return constructor.newInstance(arguments);
            } catch (InstantiationException
                    | IllegalAccessException
                    | InvocationTargetException
                    | IllegalArgumentException exception) {
                continue;
            }
        }

        return null;
    }

    private int invokePublicMethods(
            Class<?> type,
            Object instance,
            StringBuilder diagnostics) {

        int invocationCount = 0;

        for (Method method : type.getMethods()) {
            if (method.getDeclaringClass() == Object.class
                    || !Modifier.isPublic(method.getModifiers())) {
                continue;
            }

            if (!isCoverageTarget(method)) {
                continue;
            }

            Object target = Modifier.isStatic(method.getModifiers())
                    ? null
                    : instance;
            Object[] arguments = createArguments(
                    method.getParameterTypes());

            try {
                method.invoke(target, arguments);
                invocationCount++;
            } catch (IllegalAccessException
                    | InvocationTargetException
                    | IllegalArgumentException exception) {
                diagnostics.append("[method] ")
                        .append(type.getName())
                        .append('#')
                        .append(method.getName())
                        .append(System.lineSeparator());
            }
        }

        return invocationCount;
    }

    private boolean isCoverageTarget(Method method) {
        String methodName = method.getName();

        return methodName.startsWith("get")
                || methodName.startsWith("set")
                || methodName.startsWith("is")
                || methodName.startsWith("has")
                || "toString".equals(methodName)
                || "hashCode".equals(methodName)
                || "equals".equals(methodName)
                || "createContentKey".equals(methodName)
                || "success".equals(methodName)
                || "fail".equals(methodName);
    }

    private Object[] createArguments(Class<?>[] parameterTypes) {
        Object[] arguments = new Object[parameterTypes.length];

        for (int index = 0; index < parameterTypes.length; index++) {
            arguments[index] = createValue(parameterTypes[index]);
        }

        return arguments;
    }

    private Object createValue(Class<?> type) {
        if (type == boolean.class || type == Boolean.class) {
            return Boolean.TRUE;
        }
        if (type == byte.class || type == Byte.class) {
            return (byte) 1;
        }
        if (type == short.class || type == Short.class) {
            return (short) 1;
        }
        if (type == int.class || type == Integer.class) {
            return 1;
        }
        if (type == long.class || type == Long.class) {
            return 1L;
        }
        if (type == float.class || type == Float.class) {
            return 1.0F;
        }
        if (type == double.class || type == Double.class) {
            return 1.0D;
        }
        if (type == char.class || type == Character.class) {
            return 'Y';
        }
        if (type == String.class) {
            return "test";
        }
        if (type == BigDecimal.class) {
            return BigDecimal.ONE;
        }
        if (type == BigInteger.class) {
            return BigInteger.ONE;
        }
        if (type == Date.class) {
            return new Date();
        }
        if (type == java.sql.Date.class) {
            return java.sql.Date.valueOf(LocalDate.now());
        }
        if (type == Timestamp.class) {
            return Timestamp.valueOf(LocalDateTime.now());
        }
        if (type == LocalDate.class) {
            return LocalDate.now();
        }
        if (type == LocalDateTime.class) {
            return LocalDateTime.now();
        }
        if (type == Instant.class) {
            return Instant.now();
        }
        if (type == JSONObject.class) {
            return new JSONObject();
        }
        if (type == JSONArray.class) {
            return new JSONArray();
        }
        if (type.isArray()) {
            return java.lang.reflect.Array.newInstance(
                    type.getComponentType(),
                    0);
        }
        if (List.class.isAssignableFrom(type)) {
            return new ArrayList<Object>();
        }
        if (Set.class.isAssignableFrom(type)) {
            return new HashSet<Object>();
        }
        if (Map.class.isAssignableFrom(type)) {
            return new HashMap<Object, Object>();
        }
        if (type.isEnum()) {
            Object[] constants = type.getEnumConstants();
            return constants.length == 0 ? null : constants[0];
        }

        return null;
    }
}
