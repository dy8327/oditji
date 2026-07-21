<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>

<html>

<head>

<meta charset="UTF-8">

<title>채팅방 생성</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/chat-create-room.css">
</head>

<body>

<h2>채팅방 생성</h2>

<form action="${pageContext.request.contextPath}/chat/create"
      method="post">

    <!-- 테스트용 -->
    <!-- 로그인 적용 후 삭제 -->

    <input type="hidden"
           name="createdBy"
           value="1">

    <table border="1">

        <tr>

            <td width="180">

                채팅방 이름

            </td>

            <td>

                <input type="text"
                       name="roomName"
                       maxlength="100"
                       required>

            </td>

        </tr>

        <tr>

            <td>

                방 설명

            </td>

            <td>

                <textarea
                    name="roomDescription"></textarea>

            </td>

        </tr>

        <tr>

            <td>

                채팅방 종류

            </td>

            <td>

                <select name="roomType">

                    <option value="PUBLIC">

                        공개

                    </option>

                    <option value="PRIVATE">

                        비공개

                    </option>

                    <option value="NOTICE">

                        공지방

                    </option>

                </select>

            </td>

        </tr>

        <tr>

            <td>

                최대 인원

            </td>

            <td>

                <input type="text"
                       name="maxMember"
                       value="100">

            </td>

        </tr>

    </table>

    <br>

    <button type="submit">

        생성

    </button>

    <button type="button"
            onclick="history.back()">

        취소

    </button>

</form>

</body>

</html>