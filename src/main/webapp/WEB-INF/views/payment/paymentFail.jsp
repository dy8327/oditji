<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>결제 실패</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f5f6f8;
            margin: 0;
            padding: 40px;
        }

        .box {
            width: 420px;
            margin: 0 auto;
            background: #fff;
            border-radius: 16px;
            padding: 30px;
            text-align: center;
            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
        }

        h2 {
            margin-top: 0;
            color: #c0392b;
        }

        p {
            color: #555;
        }

        a {
            display: inline-block;
            margin-top: 20px;
            padding: 12px 18px;
            border-radius: 8px;
            background: #111;
            color: #fff;
            text-decoration: none;
        }
    </style>
</head>
<body>

<div class="box">
    <h2>결제에 실패했습니다.</h2>
    <p>결제 취소, 실패, 서버 검증 실패 중 하나입니다.</p>

    <a href="${pageContext.request.contextPath}/payment/test">다시 테스트하기</a>
</div>

</body>
</html>