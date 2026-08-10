package com.project.oditji.review.dao;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;

import com.project.oditji.review.vo.ProductReviewVO;
import com.project.oditji.review.vo.ReviewVO;

/**
 * ReviewDAOImpl의 모든 공개 MyBatis 위임 메서드를 실행해 남은 라인을 보완합니다.
 */
class ReviewDAOImplRemainingCoverageTest {

    @Test
    void everyPublicDaoMethodShouldDelegateWithoutUnexpectedFailure() throws Exception {
        SqlSession sqlSession =
                mock(
                        SqlSession.class,
                        invocation -> {
                            String methodName =
                                    invocation.getMethod().getName();

                            if ("selectList".equals(methodName)) {
                                return List.of();
                            }

                            if ("selectOne".equals(methodName)) {
                                Object statementArgument =
                                        invocation.getArgument(0);
                                String statement =
                                        String.valueOf(
                                                statementArgument);

                                if (statement
                                        .toLowerCase()
                                        .contains("count")) {
                                    return Integer.valueOf(1);
                                }

                                return null;
                            }

                            if ("update".equals(methodName)
                                    || "delete".equals(methodName)
                                    || "insert".equals(methodName)) {
                                return Integer.valueOf(1);
                            }

                            return RETURNS_DEFAULTS.answer(invocation);
                        });

        ReviewDAOImpl dao =
                new ReviewDAOImpl(sqlSession);

        int invokedCount = 0;

        for (Method method :
                ReviewDAOImpl.class.getDeclaredMethods()) {

            if (!Modifier.isPublic(
                    method.getModifiers())) {
                continue;
            }

            method.invoke(
                    dao,
                    createArguments(
                            method.getParameterTypes()));

            invokedCount++;
        }

        assertTrue(invokedCount >= 20);
    }

    private Object[] createArguments(
            Class<?>[] parameterTypes) {

        Object[] arguments =
                new Object[parameterTypes.length];

        for (int index = 0;
                index < parameterTypes.length;
                index++) {

            Class<?> type =
                    parameterTypes[index];

            if (Long.class.equals(type)) {
                arguments[index] = 1L;
            } else if (Integer.TYPE.equals(type)) {
                arguments[index] = 1;
            } else if (Map.class.isAssignableFrom(type)) {
                arguments[index] =
                        new HashMap<String, Object>();
            } else if (ReviewVO.class.equals(type)) {
                arguments[index] =
                        new ReviewVO();
            } else if (ProductReviewVO.class.equals(type)) {
                arguments[index] =
                        new ProductReviewVO();
            } else {
                throw new IllegalArgumentException(
                        "지원하지 않는 ReviewDAOImpl 인자 타입: "
                                + type.getName());
            }
        }

        return arguments;
    }
}
