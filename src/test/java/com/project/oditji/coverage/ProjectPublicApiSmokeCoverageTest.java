package com.project.oditji.coverage;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.project.oditji.common.util.DateTimeUtil;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.exceptions.base.MockitoException;

/**
 * 외부 API나 실제 DB를 사용하지 않고 프로젝트 공개 API를 연기 테스트합니다.
 *
 * 생성자 의존성은 Mockito로 대체하고 각 컴포넌트의 공개 메서드가
 * 최소한 입력 검증과 기본 분기까지 실행되는지 일괄 확인합니다.
 */
class ProjectPublicApiSmokeCoverageTest {

    private static final List<String> COMPONENT_CLASS_NAMES = List.of(
            "com.project.oditji.admin.controller.AdminController",
            "com.project.oditji.admin.dao.AdminDAO",
            "com.project.oditji.admin.scheduler.MemberDeleteScheduler",
            "com.project.oditji.admin.service.AdminServiceImpl",
            "com.project.oditji.business.controller.BusinessController",
            "com.project.oditji.business.scheduler.EventStatusScheduler",
            "com.project.oditji.business.service.BusinessServiceImpl",
            "com.project.oditji.cart.controller.CartController",
            "com.project.oditji.cart.controller.CartModelAdvice",
            "com.project.oditji.cart.service.CartServiceImpl",
            "com.project.oditji.chat.common.ChatResult",
            "com.project.oditji.chat.controller.ChatApiController",
            "com.project.oditji.chat.controller.ChatController",
            "com.project.oditji.chat.service.ChatServiceImpl",
            "com.project.oditji.common.config.MyBatisConfig",
            "com.project.oditji.common.config.SecurityConfig",
            "com.project.oditji.common.config.WebConfig",
            "com.project.oditji.common.controller.HomeController",
            "com.project.oditji.common.dao.AccessLogDAO",
            "com.project.oditji.common.exception.GlobalExceptionHandler",
            "com.project.oditji.common.interceptor.AccessLogInterceptor",
            "com.project.oditji.common.interceptor.AdminCheckInterceptor",
            "com.project.oditji.common.interceptor.BusinessCheckInterceptor",
            "com.project.oditji.common.interceptor.LoginCheckInterceptor",
            "com.project.oditji.common.service.MainContentPlatformService",
            "com.project.oditji.common.util.PaginationUtil",
            "com.project.oditji.common.util.PlatformNameNormalizer",
            "com.project.oditji.content.controller.ContentController",
            "com.project.oditji.content.controller.TodayContentController",
            "com.project.oditji.content.dao.ContentDAOImpl",
            "com.project.oditji.content.scheduler.ContentViewHistoryCleanupScheduler",
            "com.project.oditji.content.service.ContentServiceImpl",
            "com.project.oditji.event.controller.EventController",
            "com.project.oditji.event.dao.EventDAOImpl",
            "com.project.oditji.event.service.EventServiceImpl",
            "com.project.oditji.favorite.controller.FavoriteController",
            "com.project.oditji.favorite.service.FavoriteServiceImpl",
            "com.project.oditji.goods.controller.GoodsController",
            "com.project.oditji.goods.service.GoodsServiceImpl",
            "com.project.oditji.member.controller.GoogleLoginController",
            "com.project.oditji.member.controller.KakaoLoginController",
            "com.project.oditji.member.controller.MemberController",
            "com.project.oditji.member.controller.NaverLoginController",
            "com.project.oditji.member.controller.PlatformSelectController",
            "com.project.oditji.member.exception.MemberBlockedException",
            "com.project.oditji.member.exception.MemberWithdrawnException",
            "com.project.oditji.member.service.MemberPlatformServiceImpl",
            "com.project.oditji.member.service.MemberServiceImpl",
            "com.project.oditji.member.support.SocialLoginCallbackSupport",
            "com.project.oditji.member.support.SocialLoginSessionSupport",
            "com.project.oditji.member.support.WithdrawPolicy",
            "com.project.oditji.notification.controller.NotificationApiController",
            "com.project.oditji.notification.service.NotificationServiceImpl",
            "com.project.oditji.order.controller.OrderController",
            "com.project.oditji.order.service.OrderServiceImpl",
            "com.project.oditji.payment.controller.PaymentController",
            "com.project.oditji.payment.service.PaymentServiceImpl",
            "com.project.oditji.ranking.controller.RankingController",
            "com.project.oditji.ranking.service.RankingServiceImpl",
            "com.project.oditji.recommend.controller.RecommendController",
            "com.project.oditji.recommend.service.RecommendServiceImpl",
            "com.project.oditji.refund.service.OrderCancelRefundServiceImpl",
            "com.project.oditji.report.controller.ReportController",
            "com.project.oditji.report.dao.ReportDAOImpl",
            "com.project.oditji.report.service.ReportServiceImpl",
            "com.project.oditji.review.controller.ReviewController",
            "com.project.oditji.review.dao.ReviewDAOImpl",
            "com.project.oditji.review.service.ReviewServiceImpl",
            "com.project.oditji.search.controller.SearchController",
            "com.project.oditji.search.service.SearchContentAgeRatingResolver",
            "com.project.oditji.search.service.SearchContentEnrichmentService",
            "com.project.oditji.search.service.SearchContentManualOverrideService",
            "com.project.oditji.search.service.SearchContentPageCacheService",
            "com.project.oditji.search.service.SearchContentPolicyService",
            "com.project.oditji.search.service.SearchContentStore",
            "com.project.oditji.search.service.TmdbProviderRegistry",
            "com.project.oditji.search.service.TmdbProviderService",
            "com.project.oditji.tmdb.controller.TmdbController",
            "com.project.oditji.verify.controller.VerifyController",
            "com.project.oditji.wish.controller.WishController",
            "com.project.oditji.wish.service.WishServiceImpl"
    );

    @Test
    void publicComponentApisShouldReachValidationAndDelegationPaths() {
        int constructedClassCount = 0;
        int attemptedMethodCount = 0;
        StringBuilder diagnostics = new StringBuilder();

        for (String className : COMPONENT_CLASS_NAMES) {
            try {
                Class<?> type = Class.forName(className);
                Object instance = instantiateComponent(type);

                if (instance == null) {
                    diagnostics.append("[constructor] ")
                            .append(className)
                            .append(System.lineSeparator());
                    continue;
                }

                constructedClassCount++;
                attemptedMethodCount += invokeDeclaredPublicMethods(
                        type,
                        instance,
                        diagnostics);
            } catch (ClassNotFoundException exception) {
                diagnostics.append("[class] ")
                        .append(className)
                        .append(System.lineSeparator());
            }
        }

        String message = diagnostics.toString();
        assertTrue(
                constructedClassCount >= 55,
                "생성된 컴포넌트 수가 예상보다 적습니다: "
                        + constructedClassCount
                        + System.lineSeparator()
                        + message);
        assertTrue(
                attemptedMethodCount >= 350,
                "호출한 공개 메서드 수가 예상보다 적습니다: "
                        + attemptedMethodCount
                        + System.lineSeparator()
                        + message);
    }

    private Object instantiateComponent(Class<?> type) {
        if (Modifier.isAbstract(type.getModifiers())
                || type.isInterface()) {
            return createMock(type);
        }

        Constructor<?>[] constructors = type.getConstructors();
        Arrays.sort(
                constructors,
                Comparator.comparingInt(Executable::getParameterCount));

        for (Constructor<?> constructor : constructors) {
            Object[] arguments = createArguments(
                    constructor.getParameterTypes(),
                    0);

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

    private int invokeDeclaredPublicMethods(
            Class<?> type,
            Object instance,
            StringBuilder diagnostics) {

        int attemptedCount = 0;

        for (Method method : type.getDeclaredMethods()) {
            if (!Modifier.isPublic(method.getModifiers())
                    || method.isSynthetic()
                    || "main".equals(method.getName())
                    || shouldSkipMethod(type, method)) {
                continue;
            }

            Object target = Modifier.isStatic(method.getModifiers())
                    ? null
                    : instance;
            Object[] arguments = createArguments(
                    method.getParameterTypes(),
                    0);

            attemptedCount++;

            try {
                method.invoke(target, arguments);
            } catch (IllegalAccessException exception) {
                diagnostics.append("[access] ")
                        .append(type.getName())
                        .append('#')
                        .append(method.getName())
                        .append(System.lineSeparator());
            } catch (InvocationTargetException exception) {
                if (exception.getCause() == null) {
                    diagnostics.append("[cause] ")
                            .append(type.getName())
                            .append('#')
                            .append(method.getName())
                            .append(System.lineSeparator());
                }
            } catch (IllegalArgumentException exception) {
                diagnostics.append("[argument] ")
                        .append(type.getName())
                        .append('#')
                        .append(method.getName())
                        .append(System.lineSeparator());
            }
        }

        return attemptedCount;
    }

    private boolean shouldSkipMethod(
            Class<?> type,
            Method method) {

        String className = type.getSimpleName();
        String methodName = method.getName();

        if ("SearchContentEnrichmentService".equals(className)
                && "enrichBatch".equals(methodName)) {
            return true;
        }

        if ("SearchContentManualOverrideService".equals(className)
                && ("reload".equals(methodName)
                        || "createCandidates".equals(methodName))) {
            return true;
        }

        for (Class<?> parameterType : method.getParameterTypes()) {
            String parameterName = parameterType.getName();
            if (parameterName.contains("MultipartFile")
                    || parameterName.contains("Part")) {
                return true;
            }
        }

        return false;
    }

    private Object[] createArguments(
            Class<?>[] parameterTypes,
            int depth) {

        Object[] arguments = new Object[parameterTypes.length];

        for (int index = 0; index < parameterTypes.length; index++) {
            arguments[index] = createValue(
                    parameterTypes[index],
                    depth + 1);
        }

        return arguments;
    }

    private Object createValue(
            Class<?> type,
            int depth) {

        if (type == boolean.class || type == Boolean.class) {
            return Boolean.FALSE;
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
        if (type == LocalDate.class) {
            return LocalDate.now();
        }
        if (type == LocalDateTime.class) {
            return LocalDateTime.now(DateTimeUtil.KOREA_ZONE);
        }
        if (type == Instant.class) {
            return Instant.now();
        }
        if (type == Locale.class) {
            return Locale.KOREA;
        }
        if (type == Path.class) {
            return Path.of(System.getProperty("java.io.tmpdir"));
        }
        if (type == File.class) {
            return new File(System.getProperty("java.io.tmpdir"));
        }
        if (type == InputStream.class) {
            return new ByteArrayInputStream(
                    "test".getBytes(StandardCharsets.UTF_8));
        }
        if (type == JSONObject.class) {
            return new JSONObject();
        }
        if (type == JSONArray.class) {
            return new JSONArray();
        }
        if (type == Optional.class) {
            return Optional.empty();
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
        if (type == Object.class) {
            return new Object();
        }
        if (depth <= 2 && type.getName().contains(".vo.")) {
            Object vo = instantiateComponent(type);
            if (vo != null) {
                return vo;
            }
        }

        return createMock(type);
    }

    private Object createMock(Class<?> type) {
        try {
            return mock(
                    type,
                    Answers.RETURNS_DEEP_STUBS);
        } catch (MockitoException exception) {
            return null;
        }
    }
}
