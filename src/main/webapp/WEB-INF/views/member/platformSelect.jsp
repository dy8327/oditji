<%@ page contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>

<%@ page import="java.util.List" %>
<%@ page import="com.project.oditji.member.vo.PlatformVO" %>

<!DOCTYPE html>

<%--
    SonarQube 접근성 규칙을 충족하도록
    문서의 기본 언어를 한국어로 지정한다.
--%>
<html lang="ko">

<head>

    <meta charset="UTF-8">

    <title>
        OTT 플랫폼 선택
    </title>

</head>

<body>

<%@ include file="/WEB-INF/views/common/header.jsp"%>

<main>

    <h2>
        사용 중인 OTT 플랫폼을 선택해주세요
    </h2>

    <p>
        선택한 플랫폼을 기준으로 맞춤 콘텐츠를 보여드릴게요.
    </p>

    <%
        /*
         * Controller가 전달한 오류 메시지를 화면에 출력한다.
         * 기존 화면 동작을 유지하기 위해 기존 스크립틀릿 구조를 유지한다.
         */
        String errorMsg =
                (String) request.getAttribute(
                        "errorMsg"
                );

        if (errorMsg != null
                && !errorMsg.isEmpty()) {
    %>

        <p style="color:red;">
            <%= errorMsg %>
        </p>

    <%
        }
    %>

    <form action="<%= request.getContextPath() %>/member/platform/select"
          method="post">

        <fieldset style="border:0; padding:0; margin:0;">

            <%--
                여러 개의 OTT 체크박스를 묶는 영역이므로
                fieldset과 legend를 사용해 체크박스 그룹의 의미를 제공한다.
            --%>
            <legend style="position:absolute;
                           width:1px;
                           height:1px;
                           padding:0;
                           margin:-1px;
                           overflow:hidden;
                           clip:rect(0, 0, 0, 0);
                           white-space:nowrap;
                           border:0;">
                사용 중인 OTT 플랫폼
            </legend>

            <%
                /*
                 * Controller에서 전달한 플랫폼 목록을 순회한다.
                 * label이 input을 감싸고 있으므로 각 체크박스는
                 * 플랫폼 이름과 자동으로 연결된다.
                 */
                List<PlatformVO> platformList =
                        (List<PlatformVO>) request.getAttribute(
                                "platformList"
                        );

                if (platformList != null) {

                    for (PlatformVO platform : platformList) {
            %>

                <label style="display:block; margin-bottom:10px;">

                    <input type="checkbox"
                           name="platformNoList"
                           value="<%= platform.getPlatformNo() %>">

                    <%= platform.getPlatformName() %>

                </label>

            <%
                    }
                }
            %>

        </fieldset>

        <button type="submit">
            선택 완료
        </button>

    </form>

</main>

<%@ include file="/WEB-INF/views/common/footer.jsp"%>

</body>

</html>