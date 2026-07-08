<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<html>
  <head>
    <meta charset="utf-8" />
    <title>회원가입</title>
  </head>
  <body>
  <%@ include file="/WEB-INF/views/common/header.jsp"%>
    <div>
      <form action="/member/join" method="POST" enctype="multipart/form-data">
        
        <div>
          <label>아이디</label>
          <input type="text" name="MEMBER_ID" class="form-control" required />
        </div>
        <div>
          <label>비밀번호</label>
          <input type="password" name="MEMBER_PW" class="form-control" required />
        </div>
        <div>
          <label>이름</label>
          <input type="text" name="MEMBER_NAME" class="form-control" required />
        </div>
        <div>
          <label>닉네임</label>
          <input type="text" name="NICKNAME" class="form-control" required />
        </div>
        <div>
          <label>이메일</label>
          <input type="email" name="EMAIL" class="form-control" placeholder="example@naver.com" required />
        </div>
        <div>
          <label>전화번호</label>
          <input type="text" name="PHONE" class="form-control" placeholder="010-1234-5678" required />
        </div>
        
        <div style="margin-top: 15px;">
            <label style="font-weight: bold; margin-bottom: 8px; display: block;">사용할 플랫폼 선택</label>
            <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap;">
                
                <input type="text" 
                      name="PLATFORM_USER_ID" 
                      class="form-control" 
                      placeholder="플랫폼 계정 ID 입력" 
                      style="width: 180px; height: 38px; border: 1px solid #ccc; border-radius: 4px; padding: 0 10px;" 
                      required />
                
                <select id="platformSelect" 
                        name="PLATFORM_NO" 
                        class="form-control" 
                        style="width: 160px; height: 38px; border-radius: 4px; border: 1px solid #ccc; padding: 0 5px;" 
                        required>
                    <option value="" disabled selected hidden>플랫폼 선택</option>
                    <option value="1">디즈니플러스</option>
                    <option value="2">티빙</option>
                    <option value="3">넷픽스</option>
                    <option value="4">쿠팡플레이</option>
                    <option value="5">왓챠</option>
                </select>
                
            </div>
        </div>
        
        <div style="margin-top: 15px;">
          <label for="PROFILE_IMAGE">프로필 사진</label>
          <input type="file" id="PROFILE_IMAGE" name="PROFILE_IMAGE_FILE" class="form-control" accept="image/*" />
        </div>

        <div style="margin-top: 20px">
          <button type="submit">회원가입</button>
        </div>
      </form>
    </div>
    <%@ include file="/WEB-INF/views/common/footer.jsp"%>
  </body>
</html>