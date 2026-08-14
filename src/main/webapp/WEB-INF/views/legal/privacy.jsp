<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI - 개인정보 처리방침</title>
<jsp:include page="/WEB-INF/views/common/head-assets.jsp"/>

<link rel="stylesheet"
    href="${pageContext.request.contextPath}/css/legal.css">
</head>

<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<main id="mainContent" class="legal-page">
    <section class="legal-hero" aria-labelledby="privacyTitle">
        <span class="legal-eyebrow">PRIVACY POLICY</span>
        <h1 id="privacyTitle" class="legal-title">개인정보 처리방침</h1>
        <p class="legal-summary">
            ODITJI는 이용자의 개인정보를 중요하게 생각하며, 개인정보 보호 관련 법령의 취지에 따라
            개인정보를 안전하게 처리하고 이용자의 권리를 보호하기 위해 다음과 같이 개인정보 처리방침을 공개합니다.
        </p>
        <div class="legal-meta">
            <span>제정일: 2026년 8월 14일</span>
            <span>시행일: 2026년 8월 14일</span>
        </div>
    </section>

    <div class="legal-layout">
        <nav class="legal-toc" aria-label="개인정보 처리방침 목차">
            <strong>목차</strong>
            <a href="#privacy-purpose">1. 개인정보 처리 목적</a>
            <a href="#privacy-items">2. 처리하는 개인정보 항목</a>
            <a href="#privacy-retention">3. 보유 및 이용기간</a>
            <a href="#privacy-thirdparty">4. 제3자 제공</a>
            <a href="#privacy-outsourcing">5. 외부 서비스 및 처리위탁</a>
            <a href="#privacy-destroy">6. 개인정보 파기</a>
            <a href="#privacy-rights">7. 이용자의 권리</a>
            <a href="#privacy-auto">8. 자동 수집 정보</a>
            <a href="#privacy-safety">9. 안전성 확보조치</a>
            <a href="#privacy-child">10. 아동 및 성인인증</a>
            <a href="#privacy-contact">11. 개인정보 보호 문의</a>
            <a href="#privacy-change">12. 방침 변경</a>
        </nav>

        <div class="legal-content">
            <section id="privacy-purpose" class="legal-section">
                <h2>1. 개인정보의 처리 목적</h2>
                <p>ODITJI는 다음의 목적을 위해 필요한 범위에서 개인정보를 처리합니다.</p>
                <ul>
                    <li>회원가입, 로그인, 회원 식별 및 계정 관리</li>
                    <li>카카오·네이버·구글 소셜 로그인 연동</li>
                    <li>비밀번호 재설정 및 이메일 인증</li>
                    <li>청소년 관람불가 또는 등급정보 없음 콘텐츠 이용을 위한 성인인증</li>
                    <li>콘텐츠 찜, 리뷰, 평점, 검색 이력 및 개인화 기능 제공</li>
                    <li>상품 장바구니, 주문, 결제, 취소·환불 및 배송 처리</li>
                    <li>상품 재입고 알림 및 서비스 알림 제공</li>
                    <li>사업자 가입, 사업자등록정보 확인, 상품·주문·배송·정산 관리</li>
                    <li>사업자 커뮤니티 및 실시간 채팅 기능 제공</li>
                    <li>서비스 장애 대응, 부정 이용 방지, 접속 기록 확인 및 보안 관리</li>
                </ul>
            </section>

            <section id="privacy-items" class="legal-section">
                <h2>2. 처리하는 개인정보 항목 및 수집 방법</h2>
                <div class="legal-table-wrap">
                    <table class="legal-table">
                        <thead>
                            <tr>
                                <th scope="col">구분</th>
                                <th scope="col">처리 항목</th>
                                <th scope="col">이용 목적</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>일반 회원가입</td>
                                <td>아이디, 암호화된 비밀번호, 이름, 닉네임, 이메일, 휴대전화번호, 선호 OTT</td>
                                <td>회원 식별, 로그인, 계정 관리, 개인화 서비스 제공</td>
                            </tr>
                            <tr>
                                <td>소셜 로그인</td>
                                <td>로그인 제공자, 제공자별 이용자 식별값, 동의 범위 내 닉네임·프로필 이미지·이름 등</td>
                                <td>카카오·네이버·구글 계정 연동 및 로그인</td>
                            </tr>
                            <tr>
                                <td>성인인증</td>
                                <td>인증 식별값, 이름, 생년월일, 휴대전화번호, 성별, 성인 여부, 인증 상태 및 인증 일시</td>
                                <td>성인 콘텐츠 접근 가능 여부 확인 및 인증 이력 관리</td>
                            </tr>
                            <tr>
                                <td>주문·배송</td>
                                <td>주문자 회원번호, 수령인 이름, 수령인 연락처, 배송지 주소, 주문·배송·취소 내역</td>
                                <td>상품 주문, 배송, 취소·환불 및 분쟁 대응</td>
                            </tr>
                            <tr>
                                <td>결제</td>
                                <td>결제 식별값, 결제금액, 결제수단 유형, 결제상태, PG 제공자, 거래 식별값, 결제·취소 시각</td>
                                <td>결제 검증, 결제내역 확인, 취소·환불 처리</td>
                            </tr>
                            <tr>
                                <td>사업자 회원</td>
                                <td>상호명, 사업자등록번호, 대표자명, 개업일, 사업자등록증 파일, 은행명, 계좌번호, 예금주, 사업자 승인 상태</td>
                                <td>사업자 확인, 판매 권한 승인, 판매대금 정산</td>
                            </tr>
                            <tr>
                                <td>서비스 이용</td>
                                <td>회원번호, 접속 IP, 브라우저·기기 정보(User-Agent), 접속 URL, 접속 일시, 검색어, 콘텐츠 조회이력, 찜·리뷰·평점, 알림 설정</td>
                                <td>서비스 제공, 이용 통계, 개인화, 장애 분석 및 부정 이용 방지</td>
                            </tr>
                            <tr>
                                <td>채팅</td>
                                <td>회원·사업자 식별정보, 채팅방 정보, 채팅 내용, 전송 시각, 읽음 상태</td>
                                <td>사업자 커뮤니티 및 실시간 채팅 제공</td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <div class="legal-notice">
                    결제 과정에서 카드번호, 유효기간 등 결제수단의 원문 정보는 ODITJI 데이터베이스에 직접 저장하지 않으며,
                    결제대행 화면 및 결제 연동 서비스에서 처리합니다.
                </div>

                <h3>수집 방법</h3>
                <ul>
                    <li>회원가입, 회원정보 수정, 주문, 사업자 가입 등 이용자가 직접 입력하는 방법</li>
                    <li>카카오·네이버·구글 OAuth 로그인 과정에서 이용자가 동의한 정보를 제공받는 방법</li>
                    <li>PortOne 본인인증·결제 연동 결과를 전달받는 방법</li>
                    <li>서비스 이용 과정에서 접속기록, 검색이력 등이 자동으로 생성되는 방법</li>
                </ul>
            </section>

            <section id="privacy-retention" class="legal-section">
                <h2>3. 개인정보의 처리 및 보유기간</h2>
                <p>
                    ODITJI는 개인정보 처리 목적이 달성되면 지체 없이 해당 정보를 파기합니다.
                    다만 관계 법령에 따라 일정 기간 보존할 필요가 있는 경우에는 해당 기간 동안 별도로 보관할 수 있습니다.
                </p>

                <div class="legal-table-wrap">
                    <table class="legal-table">
                        <thead>
                            <tr>
                                <th scope="col">보존 정보</th>
                                <th scope="col">보존 기간</th>
                                <th scope="col">근거 또는 사유</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>일반 회원 계정정보</td>
                                <td>회원 탈퇴 후 7일간 복구 가능 상태로 보관 후 삭제 대상 처리</td>
                                <td>서비스의 회원 복구 정책</td>
                            </tr>
                            <tr>
                                <td>표시·광고에 관한 기록</td>
                                <td>6개월</td>
                                <td>전자상거래 관련 법령</td>
                            </tr>
                            <tr>
                                <td>계약 또는 청약철회 등에 관한 기록</td>
                                <td>5년</td>
                                <td>전자상거래 관련 법령</td>
                            </tr>
                            <tr>
                                <td>대금결제 및 재화 등의 공급에 관한 기록</td>
                                <td>5년</td>
                                <td>전자상거래 관련 법령</td>
                            </tr>
                            <tr>
                                <td>소비자 불만 또는 분쟁처리에 관한 기록</td>
                                <td>3년</td>
                                <td>전자상거래 관련 법령</td>
                            </tr>
                            <tr>
                                <td>비회원 구독 조합 계산 공유 결과</td>
                                <td>생성 후 30일</td>
                                <td>공유 링크 제공 및 자동 정리 정책</td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <div class="legal-notice">
                    사업자 회원의 경우 주문·배송·정산 등 거래 관계가 존재할 수 있으므로 일반회원과 동일한 방식으로 즉시 탈퇴 처리되지 않을 수 있으며,
                    거래 종료 및 관계 법령상 보존 의무를 고려하여 별도로 처리할 수 있습니다.
                </div>
            </section>

            <section id="privacy-thirdparty" class="legal-section">
                <h2>4. 개인정보의 제3자 제공</h2>
                <p>
                    ODITJI는 원칙적으로 이용자의 개인정보를 수집·이용 목적의 범위를 넘어 제3자에게 제공하지 않습니다.
                    다만 이용자가 사전에 동의한 경우, 법령에 특별한 규정이 있는 경우 또는 적법한 절차에 따른 요청이 있는 경우에는 예외로 합니다.
                </p>
            </section>

            <section id="privacy-outsourcing" class="legal-section">
                <h2>5. 외부 서비스 이용 및 개인정보 처리위탁</h2>
                <p>
                    ODITJI는 서비스 제공을 위해 아래와 같은 외부 서비스를 연동할 수 있습니다.
                    실제 운영 환경의 계약·설정에 따라 세부 수탁자 또는 처리 범위가 달라지는 경우 변경사항을 본 방침에 반영합니다.
                </p>

                <div class="legal-table-wrap">
                    <table class="legal-table">
                        <thead>
                            <tr>
                                <th scope="col">외부 서비스</th>
                                <th scope="col">이용 목적</th>
                                <th scope="col">처리되는 정보 예시</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>PortOne</td>
                                <td>본인·성인인증, 결제 및 결제 취소 연동</td>
                                <td>인증 식별정보, 결제 식별정보, 결제상태 등</td>
                            </tr>
                            <tr>
                                <td>Google Firebase / Firestore</td>
                                <td>사업자 실시간 채팅 제공</td>
                                <td>채팅방 정보, 메시지, 전송 시각, 사용자 식별에 필요한 정보</td>
                            </tr>
                            <tr>
                                <td>Google Gmail SMTP</td>
                                <td>비밀번호 재설정 등 이메일 발송</td>
                                <td>수신 이메일 주소, 인증·안내 메일 내용</td>
                            </tr>
                            <tr>
                                <td>카카오·네이버·구글 OAuth</td>
                                <td>소셜 로그인 및 계정 연동</td>
                                <td>제공자별 이용자 식별값 및 이용자 동의 범위의 프로필 정보</td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <div class="legal-notice">
                    TMDB 등 콘텐츠 정보 제공 API는 콘텐츠 검색·상세정보 제공을 위한 목적으로 이용하며,
                    ODITJI 회원의 계정정보를 콘텐츠 검색 목적으로 해당 API에 제공하지 않습니다.
                </div>
            </section>

            <section id="privacy-destroy" class="legal-section">
                <h2>6. 개인정보의 파기 절차 및 방법</h2>
                <ol>
                    <li>개인정보의 보유기간이 경과하거나 처리 목적이 달성된 경우 파기 대상 정보를 확인합니다.</li>
                    <li>관계 법령에 따라 보존이 필요한 정보는 일반 이용정보와 분리하여 해당 기간 동안 보관합니다.</li>
                    <li>전자적 파일 형태의 개인정보는 복구 또는 재생이 어렵도록 삭제하며, 출력물 등은 분쇄 또는 이에 준하는 방법으로 파기합니다.</li>
                </ol>
            </section>

            <section id="privacy-rights" class="legal-section">
                <h2>7. 이용자의 권리와 행사 방법</h2>
                <p>이용자는 자신의 개인정보에 대해 다음과 같은 권리를 행사할 수 있습니다.</p>
                <ul>
                    <li>개인정보 열람 및 확인</li>
                    <li>개인정보 수정 및 정정</li>
                    <li>회원 탈퇴 및 개인정보 처리 정지 요청</li>
                    <li>선호 OTT, 알림 수신 설정 등 선택정보 변경</li>
                    <li>관련 법령이 허용하는 범위에서 개인정보 삭제 요청</li>
                </ul>
                <p>
                    회원정보 수정 및 탈퇴 등 일부 기능은 마이페이지에서 직접 처리할 수 있으며,
                    직접 처리가 어려운 사항은 개인정보 보호 문의 창구를 통해 요청할 수 있습니다.
                </p>
            </section>

            <section id="privacy-auto" class="legal-section">
                <h2>8. 자동으로 수집되는 정보</h2>
                <p>
                    서비스 이용 과정에서 접속 IP, User-Agent, 접속 URL, 접속 일시와 같은 정보가 자동으로 생성되어
                    서비스 안정성 확인, 장애 대응 및 부정 이용 방지를 위해 처리될 수 있습니다.
                </p>
                <p>
                    로그인 세션 유지를 위해 세션 식별정보가 사용될 수 있으며, 화면 테마 설정 등 일부 사용자 환경설정은
                    브라우저 저장소(Local Storage)에 저장될 수 있습니다.
                </p>
            </section>

            <section id="privacy-safety" class="legal-section">
                <h2>9. 개인정보의 안전성 확보조치</h2>
                <p>ODITJI는 개인정보의 안전한 처리를 위해 다음과 같은 기술적·관리적 조치를 적용합니다.</p>
                <ul>
                    <li>비밀번호 BCrypt 단방향 암호화 저장</li>
                    <li>Spring Security 기반 CSRF 보호</li>
                    <li>USER·BUSINESS·ADMIN 역할과 승인 상태에 따른 접근 제어</li>
                    <li>운영환경 HTTPS 적용 및 민감 설정값의 환경변수 분리</li>
                    <li>403·404·500 오류 안내화면을 통한 내부 오류정보 노출 최소화</li>
                    <li>관리자 처리 및 접속기록을 통한 운영 상태 확인</li>
                </ul>
            </section>

            <section id="privacy-child" class="legal-section">
                <h2>10. 아동의 개인정보 및 성인인증</h2>
                <p>
                    ODITJI는 콘텐츠 연령등급에 따라 청소년 관람불가 또는 등급정보 없음 콘텐츠에 대해 성인인증 절차를 적용할 수 있습니다.
                    성인인증 결과는 성인 여부 확인 및 인증 이력 관리 목적으로 사용됩니다.
                </p>
                <p>
                    법정대리인의 동의가 필요한 연령의 이용자에 대해서는 관련 법령이 요구하는 절차가 우선 적용됩니다.
                </p>
            </section>

            <section id="privacy-contact" class="legal-section">
                <h2>11. 개인정보 보호 관련 문의</h2>
                <p>
                    개인정보 처리와 관련한 문의, 열람·정정·삭제 요청 또는 불만사항은 아래 개인정보 보호 담당 창구를 통해 문의할 수 있습니다.
                </p>
                <div class="legal-contact">
                    <div>
                        <span>개인정보 보호 담당</span>
                        <strong>ODITJI 운영팀</strong>
                    </div>
                    <div>
                        <span>문의 방법</span>
                        <strong>서비스 하단 고객센터</strong>
                    </div>
                </div>
            </section>

            <section id="privacy-change" class="legal-section">
                <h2>12. 개인정보 처리방침의 변경</h2>
                <p>
                    본 개인정보 처리방침의 내용이 변경되는 경우 시행일 이전에 서비스 내 공지 또는 본 페이지를 통해 변경 내용을 안내합니다.
                    중요한 변경사항이 있는 경우에는 이용자가 변경 내용을 명확하게 확인할 수 있도록 별도로 알릴 수 있습니다.
                </p>
                <p><strong>본 방침은 2026년 8월 14일부터 시행합니다.</strong></p>
            </section>
        </div>
    </div>

    <p class="legal-project-note">
        본 페이지는 ODITJI 서비스의 현재 구현 범위를 기준으로 작성된 프로젝트용 개인정보 처리방침입니다.
        실제 상용 서비스로 운영하는 경우 운영주체, 연락처, 수탁자, 국외이전 여부 및 실제 보관정책을 최종 확인하여 수정해야 합니다.
    </p>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
