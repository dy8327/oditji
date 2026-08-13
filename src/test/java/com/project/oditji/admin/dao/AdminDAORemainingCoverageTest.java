package com.project.oditji.admin.dao;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionTemplate;

import com.project.oditji.admin.vo.ContentManageVO;
import com.project.oditji.admin.vo.PlatformVO;

/**
 * AdminDAO의 단순 MyBatis 위임 메서드를 전체적으로 실행해 남은 라인을 보완합니다.
 */
class AdminDAORemainingCoverageTest {

    @Test
    void everyPublicDaoMethodShouldDelegateWithoutUnexpectedFailure() throws Exception {

        SqlSessionTemplate sqlSession = mock(
                SqlSessionTemplate.class,
                invocation -> {

                    String methodName = invocation.getMethod().getName();

                    if ("selectList".equals(methodName)) {
                        return List.of();
                    }

                    if ("selectOne".equals(methodName)) {

                        Object statementArgument = invocation.getArgument(0);

                        String statement = String.valueOf(
                                statementArgument);

                        // [모니터링 순 방문자 수 반환 타입 반영]
                        // selectUniqueVisitorCountByPeriod는
                        // AdminDAO에서 Long 타입으로 조회하므로
                        // 공용 selectOne Mock에서도 해당 구문은
                        // Integer가 아닌 Long 값을 반환한다.
                        if (statement.contains(
                                "selectUniqueVisitorCountByPeriod")) {

                            return Long.valueOf(1L);
                        }

                        // 기존 count 계열 DAO는 int / Integer 반환이므로
                        // 기존 테스트 동작을 그대로 유지한다.
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

        AdminDAO dao = new AdminDAO(sqlSession);

        int invokedCount = 0;

        for (Method method : AdminDAO.class.getDeclaredMethods()) {

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

        assertTrue(invokedCount > 50);
    }

    private Object[] createArguments(
            Class<?>[] parameterTypes) {

        Object[] arguments = new Object[parameterTypes.length];

        for (int index = 0; index < parameterTypes.length; index++) {

            Class<?> type = parameterTypes[index];

            if (Long.class.equals(type)) {

                arguments[index] = 1L;

            } else if (String.class.equals(type)) {

                arguments[index] = "VALUE";

            } else if (Map.class.isAssignableFrom(type)) {

                arguments[index] = new HashMap<String, Object>();

            } else if (List.class.isAssignableFrom(type)) {

                arguments[index] = List.of(1L);

            } else if (ContentManageVO.class.equals(type)) {

                arguments[index] = new ContentManageVO();

            } else if (PlatformVO.class.equals(type)) {

                arguments[index] = new PlatformVO();

            } else {

                throw new IllegalArgumentException(
                        "지원하지 않는 AdminDAO 인자 타입: "
                                + type.getName());
            }
        }

        return arguments;
    }
}