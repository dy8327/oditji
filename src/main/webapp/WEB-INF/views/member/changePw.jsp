<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="ko">

    <head>

        <meta charset="UTF-8">

        <title>ODITJI - 비밀번호 변경</title>

        <link rel="stylesheet"
        href="${pageContext.request.contextPath}/css/member.css">

    </head>


    <body>


        <jsp:include page="/WEB-INF/views/common/header.jsp" />


        <div class="auth-container">

            <div class="auth-box large">


                <h2>비밀번호 변경</h2>


                <form action="${pageContext.request.contextPath}/member/changePw"
                method="post">


                <div class="form-group">

                    <label>
                        새 비밀번호
                    </label>

                    <input type="password"
                    name="newPassword"
                    required>

                </div>



                <div class="form-group">

                    <label>
                        새 비밀번호 확인
                    </label>

                    <input type="password"
                    name="confirmPassword"
                    required>

                </div>



                <button type="submit"
                class="btn-primary">

                비밀번호 변경

            </button>


        </form>


    </div>

</div>


<jsp:include page="/WEB-INF/views/common/footer.jsp" />


</body>

</html>