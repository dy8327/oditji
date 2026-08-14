<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI - 이용약관</title>
<jsp:include page="/WEB-INF/views/common/head-assets.jsp"/>

<style>
.legal-page {
    width: min(1120px, calc(100% - 40px));
    margin: 0 auto;
    padding: 56px 0 80px;
}

.legal-hero {
    margin-bottom: 30px;
    padding: 34px 36px;
    border: 1px solid var(--color-border);
    border-radius: 18px;
    background: var(--color-bg2);
}

.legal-eyebrow {
    display: inline-block;
    margin-bottom: 10px;
    color: var(--color-point);
    font-size: 13px;
    font-weight: 800;
    letter-spacing: .08em;
}

.legal-title {
    margin: 0;
    color: var(--color-white);
    font-size: clamp(28px, 4vw, 40px);
    line-height: 1.2;
}

.legal-summary {
    margin-top: 14px;
    color: var(--color-gray);
    font-size: 15px;
    line-height: 1.8;
}

.legal-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 8px 18px;
    margin-top: 18px;
    color: var(--color-gray2);
    font-size: 13px;
}

.legal-layout {
    display: grid;
    grid-template-columns: 220px minmax(0, 1fr);
    gap: 28px;
    align-items: start;
}

.legal-toc {
    position: sticky;
    top: calc(var(--header-height) + 20px);
    max-height: calc(100vh - var(--header-height) - 40px);
    overflow-y: auto;
    padding: 20px;
    border: 1px solid var(--color-border);
    border-radius: 16px;
    background: var(--color-bg2);
}

.legal-toc strong {
    display: block;
    margin-bottom: 12px;
    color: var(--color-white);
    font-size: 15px;
}

.legal-toc a {
    display: block;
    padding: 6px 0;
    color: var(--color-gray2);
    font-size: 13px;
    line-height: 1.45;
}

.legal-toc a:hover,
.legal-toc a:focus {
    color: var(--color-point);
}

.legal-content {
    min-width: 0;
}

.legal-section {
    margin-bottom: 18px;
    padding: 28px 30px;
    border: 1px solid var(--color-border);
    border-radius: 16px;
    background: var(--color-bg2);
}

.legal-section h2 {
    margin: 0 0 16px;
    color: var(--color-white);
    font-size: 21px;
    line-height: 1.4;
}

.legal-section p,
.legal-section li {
    color: var(--color-gray);
    font-size: 14px;
    line-height: 1.85;
}

.legal-section ul,
.legal-section ol {
    padding-left: 20px;
}

.legal-section ul {
    list-style: disc;
}

.legal-section ol {
    list-style: decimal;
}

.legal-section li + li {
    margin-top: 6px;
}

.legal-notice {
    margin-top: 14px;
    padding: 14px 16px;
    border-left: 3px solid var(--color-point);
    border-radius: 8px;
    background: var(--color-point-a15);
    color: var(--color-gray);
    font-size: 13px;
    line-height: 1.75;
}

.legal-project-note {
    margin-top: 28px;
    color: var(--color-gray2);
    font-size: 12px;
    line-height: 1.7;
    text-align: center;
}

@media (max-width: 900px) {
    .legal-layout {
        grid-template-columns: 1fr;
    }

    .legal-toc {
        position: static;
        max-height: none;
    }
}

@media (max-width: 600px) {
    .legal-page {
        width: min(100% - 24px, 1120px);
        padding: 28px 0 52px;
    }

    .legal-hero,
    .legal-section {
        padding: 22px 18px;
        border-radius: 14px;
    }
}
</style>
</head>

<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<main id="mainContent" class="legal-page">
    <section class="legal-hero" aria-labelledby="termsTitle">
        <span class="legal-eyebrow">TERMS OF SERVICE</span>
        <h1 id="termsTitle" class="legal-title">ODITJI 이용약관</h1>
        <p class="legal-summary">
            본 약관은 ODITJI가 제공하는 OTT 통합검색, 콘텐츠 정보, 굿즈 연계, 주문·결제 및 기타 관련 서비스의
            이용조건과 이용자 및 서비스 운영주체의 권리·의무를 정합니다.
        </p>
        <div class="legal-meta">
            <span>제정일: 2026년 8월 14일</span>
            <span>시행일: 2026년 8월 14일</span>
        </div>
    </section>

    <div class="legal-layout">
        <nav class="legal-toc" aria-label="이용약관 목차">
            <strong>목차</strong>
            <a href="#terms-1">제1조 목적</a>
            <a href="#terms-2">제2조 용어의 정의</a>
            <a href="#terms-3">제3조 약관의 게시 및 변경</a>
            <a href="#terms-4">제4조 서비스의 내용</a>
            <a href="#terms-5">제5조 외부 콘텐츠 정보</a>
            <a href="#terms-6">제6조 회원가입</a>
            <a href="#terms-7">제7조 계정 관리</a>
            <a href="#terms-8">제8조 회원 탈퇴 및 이용제한</a>
            <a href="#terms-9">제9조 성인인증</a>
            <a href="#terms-10">제10조 이용자의 의무</a>
            <a href="#terms-11">제11조 상품 정보</a>
            <a href="#terms-12">제12조 주문 및 계약</a>
            <a href="#terms-13">제13조 결제</a>
            <a href="#terms-14">제14조 배송</a>
            <a href="#terms-15">제15조 취소·환불</a>
            <a href="#terms-16">제16조 리뷰·신고</a>
            <a href="#terms-17">제17조 채팅·커뮤니티</a>
            <a href="#terms-18">제18조 사업자 회원</a>
            <a href="#terms-19">제19조 서비스 변경·중단</a>
            <a href="#terms-20">제20조 지식재산권</a>
            <a href="#terms-21">제21조 책임의 제한</a>
            <a href="#terms-22">제22조 분쟁 해결</a>
            <a href="#terms-23">제23조 시행일</a>
        </nav>

        <div class="legal-content">
            <section id="terms-1" class="legal-section">
                <h2>제1조 (목적)</h2>
                <p>
                    본 약관은 ODITJI(이하 “서비스”)가 제공하는 OTT 콘텐츠 통합검색, 콘텐츠 상세정보,
                    콘텐츠 관련 상품 탐색, 회원 기능, 주문·결제, 사업자 기능 및 기타 부가서비스의 이용과 관련하여
                    서비스와 이용자 사이의 권리, 의무 및 책임사항을 정하는 것을 목적으로 합니다.
                </p>
            </section>

            <section id="terms-2" class="legal-section">
                <h2>제2조 (용어의 정의)</h2>
                <ol>
                    <li>“이용자”란 본 약관에 따라 서비스를 이용하는 회원 및 비회원을 말합니다.</li>
                    <li>“회원”이란 회원가입 또는 소셜 로그인을 통해 계정을 생성하고 서비스를 이용하는 자를 말합니다.</li>
                    <li>“사업자 회원”이란 사업자정보를 등록하고 관리자 승인 후 상품 판매·주문·배송·정산 관련 기능을 이용하는 회원을 말합니다.</li>
                    <li>“콘텐츠”란 영화, TV 프로그램 등 서비스에서 제공하는 미디어 관련 정보를 말합니다.</li>
                    <li>“상품”이란 콘텐츠와 연계되어 등록되는 굿즈, 의류, 도서, 소품 등 판매 대상 정보를 말합니다.</li>
                    <li>“외부 서비스”란 TMDB, OTT 제공처, PortOne, 소셜 로그인 제공자, Firebase 등 서비스와 연동되는 제3자의 시스템을 말합니다.</li>
                </ol>
            </section>

            <section id="terms-3" class="legal-section">
                <h2>제3조 (약관의 게시 및 변경)</h2>
                <ol>
                    <li>서비스는 이용자가 쉽게 확인할 수 있도록 본 약관을 서비스 화면 또는 하단 영역에 게시합니다.</li>
                    <li>관계 법령을 위반하지 않는 범위에서 약관을 변경할 수 있으며, 중요한 변경사항은 시행 전에 서비스 내에서 안내합니다.</li>
                    <li>변경된 약관은 별도로 정한 시행일부터 효력이 발생합니다.</li>
                </ol>
            </section>

            <section id="terms-4" class="legal-section">
                <h2>제4조 (서비스의 내용)</h2>
                <p>ODITJI는 다음과 같은 기능을 제공합니다.</p>
                <ul>
                    <li>영화·TV 콘텐츠 통합검색, 장르·OTT 필터, 랭킹 및 상세정보 제공</li>
                    <li>OTT 제공처 정보, 공개 예정 콘텐츠, OTT 할인정보 및 구독 조합 계산 기능</li>
                    <li>콘텐츠 찜, 리뷰, 평점, 개인화 추천 및 검색 이력 기능</li>
                    <li>관련 상품 탐색, 옵션 선택, 장바구니, 주문, 결제, 배송조회 및 취소·환불 기능</li>
                    <li>상품 재입고 알림 및 서비스 알림 기능</li>
                    <li>사업자 상품·재고·주문·배송·정산·이벤트 관리 기능</li>
                    <li>사업자 커뮤니티 및 실시간 채팅 기능</li>
                    <li>관리자 회원·사업자·상품·리뷰·신고·주문·정산 관리 기능</li>
                </ul>
            </section>

            <section id="terms-5" class="legal-section">
                <h2>제5조 (외부 콘텐츠 및 OTT 제공정보)</h2>
                <ol>
                    <li>콘텐츠 제목, 이미지, 출연진, 평점, OTT 제공처 등 일부 정보는 TMDB 등 외부 데이터 또는 API를 기반으로 제공될 수 있습니다.</li>
                    <li>OTT 시청 가능 여부, 요금, 할인정보, 공개일 등은 외부 사업자의 정책 변경이나 데이터 갱신 시점에 따라 실제 정보와 차이가 발생할 수 있습니다.</li>
                    <li>이용자는 최종 결제 또는 구독 전에 해당 OTT·제휴사·판매처의 공식 정보를 확인해야 합니다.</li>
                    <li>서비스는 외부 서비스가 제공하는 데이터의 중단, 변경, 지연 또는 오류로 인해 일부 기능을 일시적으로 제공하지 못할 수 있습니다.</li>
                </ol>
                <div class="legal-notice">
                    ODITJI가 표시하는 콘텐츠·OTT·할인 정보는 탐색을 돕기 위한 정보이며,
                    실제 제공 조건과 가격은 각 공식 서비스의 최신 안내가 우선합니다.
                </div>
            </section>

            <section id="terms-6" class="legal-section">
                <h2>제6조 (회원가입 및 계정 생성)</h2>
                <ol>
                    <li>이용자는 서비스가 정한 가입 절차에 따라 필요한 정보를 입력하여 회원가입을 신청할 수 있습니다.</li>
                    <li>카카오·네이버·구글 등 지원되는 외부 계정을 이용한 소셜 로그인을 통해 계정을 생성할 수 있습니다.</li>
                    <li>허위정보 입력, 타인 명의 도용 또는 서비스 운영을 방해할 목적이 확인되는 경우 가입이 제한되거나 계정 이용이 제한될 수 있습니다.</li>
                    <li>사업자 회원은 일반 회원가입 정보 외에 사업자정보 및 정산정보를 제출해야 하며 관리자 승인 후 사업자 전용 기능을 이용할 수 있습니다.</li>
                </ol>
            </section>

            <section id="terms-7" class="legal-section">
                <h2>제7조 (계정 및 회원정보 관리)</h2>
                <ol>
                    <li>회원은 자신의 계정정보를 정확하게 유지해야 하며 변경사항이 있는 경우 수정해야 합니다.</li>
                    <li>회원은 아이디와 비밀번호 등 인증수단을 제3자에게 양도하거나 공유해서는 안 됩니다.</li>
                    <li>비밀번호는 안전한 방식으로 관리해야 하며 계정 도용이 의심되는 경우 즉시 비밀번호를 변경하거나 서비스 운영자에게 알려야 합니다.</li>
                    <li>서비스는 보안을 위해 비정상적인 접근이나 권한 없는 접근을 제한할 수 있습니다.</li>
                </ol>
            </section>

            <section id="terms-8" class="legal-section">
                <h2>제8조 (회원 탈퇴 및 이용제한)</h2>
                <ol>
                    <li>일반 회원은 마이페이지에서 회원 탈퇴를 신청할 수 있습니다.</li>
                    <li>일반 회원의 자진 탈퇴 후 일정 기간 동안 계정 복구가 가능할 수 있으며, 현재 서비스 정책상 복구 가능 기간은 탈퇴일로부터 7일입니다.</li>
                    <li>사업자 회원은 미처리 주문, 배송, 환불, 정산 등 거래 관련 데이터가 존재하는 경우 일반 회원과 동일한 방법으로 즉시 탈퇴할 수 없을 수 있습니다.</li>
                    <li>서비스는 약관 위반, 부정 이용, 타인 권리 침해 또는 운영 방해가 확인된 회원에 대해 이용을 제한하거나 계정을 차단할 수 있습니다.</li>
                </ol>
            </section>

            <section id="terms-9" class="legal-section">
                <h2>제9조 (성인인증 및 연령 제한 콘텐츠)</h2>
                <ol>
                    <li>청소년 관람불가 또는 등급정보가 확인되지 않은 일부 콘텐츠는 성인인증 후 이용할 수 있습니다.</li>
                    <li>성인인증은 서비스가 연동한 본인인증 절차를 통해 진행될 수 있습니다.</li>
                    <li>이용자는 타인의 명의 또는 인증수단을 이용하여 연령 제한을 우회해서는 안 됩니다.</li>
                    <li>관계 법령 또는 외부 인증 서비스의 정책에 따라 인증방식이나 이용 가능 범위가 변경될 수 있습니다.</li>
                </ol>
            </section>

            <section id="terms-10" class="legal-section">
                <h2>제10조 (이용자의 의무)</h2>
                <p>이용자는 다음 행위를 해서는 안 됩니다.</p>
                <ul>
                    <li>타인의 개인정보 또는 계정을 도용하는 행위</li>
                    <li>서비스 또는 외부 시스템에 비정상적인 요청을 반복하여 장애를 유발하는 행위</li>
                    <li>허위 리뷰, 허위 신고, 스팸, 광고성 또는 불법적인 내용을 게시하는 행위</li>
                    <li>다른 이용자 또는 사업자를 모욕·협박하거나 권리를 침해하는 행위</li>
                    <li>서비스의 소스, 데이터, 화면 또는 기능을 권한 없이 복제·변조·배포하는 행위</li>
                    <li>기타 관계 법령 또는 사회질서에 위반되는 행위</li>
                </ul>
            </section>

            <section id="terms-11" class="legal-section">
                <h2>제11조 (상품 정보 및 재고)</h2>
                <ol>
                    <li>상품의 가격, 할인율, 옵션, 재고, 이미지 및 상세설명은 등록된 상품정보를 기준으로 표시됩니다.</li>
                    <li>색상·사이즈 등 옵션별 재고가 존재하는 상품은 선택한 옵션의 재고를 기준으로 주문 가능 여부가 결정됩니다.</li>
                    <li>재고가 소진된 상품 또는 옵션에 대해서는 재입고 알림 신청 기능이 제공될 수 있습니다.</li>
                    <li>상품정보에 오류가 있거나 관리자 승인 상태가 변경된 경우 상품 노출 또는 주문이 제한될 수 있습니다.</li>
                </ol>
            </section>

            <section id="terms-12" class="legal-section">
                <h2>제12조 (주문 및 계약의 성립)</h2>
                <ol>
                    <li>회원은 상품, 옵션, 수량, 배송정보 및 최종 결제금액을 확인한 후 주문을 신청합니다.</li>
                    <li>서비스는 주문 처리 과정에서 실제 상품가격, 수량, 재고 및 결제금액을 다시 확인할 수 있습니다.</li>
                    <li>재고 부족, 상품정보 오류, 결제 실패 등 정상적인 주문 처리가 어려운 사유가 있는 경우 주문이 성립되지 않거나 취소될 수 있습니다.</li>
                    <li>주문이 정상적으로 완료된 경우 주문내역을 통해 주문 상태를 확인할 수 있습니다.</li>
                </ol>
            </section>

            <section id="terms-13" class="legal-section">
                <h2>제13조 (결제)</h2>
                <ol>
                    <li>상품 결제는 서비스에 연동된 전자결제 서비스를 통해 처리됩니다.</li>
                    <li>서비스는 결제 완료 후 결제대행 서비스의 결제정보를 조회하여 주문금액과 실제 결제금액이 일치하는지 검증할 수 있습니다.</li>
                    <li>결제 과정에서 카드번호 등 결제수단의 민감한 원문 정보는 해당 결제 서비스에서 처리되며 ODITJI가 직접 저장하지 않습니다.</li>
                    <li>결제 서비스 장애, 금융기관 점검 또는 외부 시스템 오류가 발생한 경우 결제가 지연되거나 실패할 수 있습니다.</li>
                </ol>
            </section>

            <section id="terms-14" class="legal-section">
                <h2>제14조 (배송)</h2>
                <ol>
                    <li>주문 상품의 배송은 주문 시 입력한 수령인 및 배송지 정보를 기준으로 처리합니다.</li>
                    <li>사업자 회원은 주문 확인 후 택배사와 운송장정보를 입력하고 배송 상태를 갱신할 수 있습니다.</li>
                    <li>배송업체 사정, 천재지변, 주소 오류 등 서비스가 통제하기 어려운 사유로 배송이 지연될 수 있습니다.</li>
                    <li>이용자의 잘못된 배송정보 입력으로 발생한 문제는 이용자에게 책임이 있을 수 있습니다.</li>
                </ol>
            </section>

            <section id="terms-15" class="legal-section">
                <h2>제15조 (주문 취소, 청약철회 및 환불)</h2>
                <ol>
                    <li>회원은 주문 상태 및 관계 법령이 허용하는 범위에서 전체 또는 일부 상품의 취소를 요청할 수 있습니다.</li>
                    <li>배송 완료 상품의 청약철회 가능 기간과 제한 사유는 전자상거래 관련 법령 및 개별 상품의 특성에 따릅니다.</li>
                    <li>이용자의 책임으로 상품이 훼손되거나 사용으로 가치가 현저히 감소하는 등 법령상 청약철회 제한 사유가 있는 경우 취소·환불이 제한될 수 있습니다.</li>
                    <li>취소 요청이 승인되면 결제수단 및 결제대행사의 절차에 따라 전액 또는 부분 환불이 진행됩니다.</li>
                    <li>환불 완료 시점은 결제수단, 카드사, 금융기관 및 결제대행사의 처리 일정에 따라 달라질 수 있습니다.</li>
                </ol>
            </section>

            <section id="terms-16" class="legal-section">
                <h2>제16조 (리뷰, 평점 및 신고)</h2>
                <ol>
                    <li>회원은 서비스 정책에 따라 콘텐츠 리뷰·평점 또는 구매 상품 리뷰를 작성할 수 있습니다.</li>
                    <li>스포일러가 포함된 리뷰는 서비스에서 제공하는 스포일러 표시 기능을 적절히 이용해야 합니다.</li>
                    <li>욕설, 혐오, 불법정보, 광고, 허위내용, 개인정보 노출 또는 타인의 권리를 침해하는 게시물은 신고 또는 관리자 검토 대상이 될 수 있습니다.</li>
                    <li>관리자는 신고된 리뷰를 검토하여 상태를 변경하거나 필요한 조치를 할 수 있습니다.</li>
                </ol>
            </section>

            <section id="terms-17" class="legal-section">
                <h2>제17조 (채팅 및 커뮤니티 이용)</h2>
                <ol>
                    <li>사업자 커뮤니티와 채팅은 승인된 이용자에게 제공될 수 있으며, 이용 권한은 회원 역할과 승인 상태에 따라 달라질 수 있습니다.</li>
                    <li>이용자는 채팅에서 개인정보, 불법정보, 악성코드, 반복 광고 또는 타인을 침해하는 내용을 전송해서는 안 됩니다.</li>
                    <li>서비스 운영 및 분쟁 대응을 위해 채팅방 참여정보와 읽음 상태 등이 기록될 수 있습니다.</li>
                    <li>운영정책 위반이 확인된 경우 채팅방 이용이 제한될 수 있습니다.</li>
                </ol>
            </section>

            <section id="terms-18" class="legal-section">
                <h2>제18조 (사업자 회원의 의무)</h2>
                <ol>
                    <li>사업자 회원은 정확한 사업자정보, 상품정보, 재고, 배송정보 및 정산정보를 등록해야 합니다.</li>
                    <li>상품 등록·수정·삭제 요청은 서비스의 관리자 검토 절차에 따라 승인 또는 반려될 수 있습니다.</li>
                    <li>사업자 회원은 자신의 상품 주문, 배송, 취소, 환불 및 정산 업무를 성실하게 처리해야 합니다.</li>
                    <li>허위 사업자정보, 허위 상품정보, 반복적인 미배송 등 서비스 신뢰를 훼손하는 행위가 확인되는 경우 사업자 권한이 제한될 수 있습니다.</li>
                </ol>
            </section>

            <section id="terms-19" class="legal-section">
                <h2>제19조 (서비스의 변경 및 중단)</h2>
                <ol>
                    <li>서비스는 운영 또는 기술상 필요에 따라 기능, 화면, 제공방식 또는 지원 외부 서비스를 변경할 수 있습니다.</li>
                    <li>시스템 점검, 서버 장애, 외부 API 장애, 네트워크 장애, 보안상 긴급조치 등이 필요한 경우 서비스의 전부 또는 일부가 일시 중단될 수 있습니다.</li>
                    <li>예측 가능한 중대한 중단이 있는 경우 가능한 범위에서 사전에 안내합니다.</li>
                </ol>
            </section>

            <section id="terms-20" class="legal-section">
                <h2>제20조 (지식재산권)</h2>
                <ol>
                    <li>ODITJI가 자체 제작한 서비스 화면, 로고, 프로그램 코드 및 편집물에 대한 권리는 각 권리자에게 귀속됩니다.</li>
                    <li>TMDB 이미지·정보, OTT 로고, 외부 서비스 상표 및 기타 제3자 콘텐츠에 대한 권리는 해당 권리자에게 귀속됩니다.</li>
                    <li>이용자는 권리자의 허락 없이 서비스의 콘텐츠를 영리 목적으로 복제·배포·변형해서는 안 됩니다.</li>
                </ol>
            </section>

            <section id="terms-21" class="legal-section">
                <h2>제21조 (책임의 제한)</h2>
                <ol>
                    <li>천재지변, 통신망 장애, 외부 API 또는 제3자 서비스 장애 등 합리적으로 통제하기 어려운 사유로 서비스를 제공할 수 없는 경우 그 범위에서 책임이 제한될 수 있습니다.</li>
                    <li>서비스가 제공하는 OTT 제공처·가격·할인·콘텐츠 정보는 외부 데이터의 갱신 시점에 따라 실제 정보와 차이가 있을 수 있습니다.</li>
                    <li>이용자의 귀책사유로 발생한 계정 도용, 잘못된 정보 입력 또는 약관 위반에 대해서는 서비스의 책임이 제한될 수 있습니다.</li>
                    <li>본 조는 관계 법령에 따라 서비스가 부담해야 하는 책임을 임의로 배제하지 않습니다.</li>
                </ol>
            </section>

            <section id="terms-22" class="legal-section">
                <h2>제22조 (분쟁 해결 및 준거법)</h2>
                <ol>
                    <li>서비스와 이용자는 서비스 이용과 관련한 분쟁이 발생한 경우 상호 협의를 통해 원만하게 해결하도록 노력합니다.</li>
                    <li>협의로 해결되지 않는 분쟁은 대한민국 법령을 기준으로 처리하며, 관할법원은 관련 법령이 정하는 바에 따릅니다.</li>
                    <li>전자상거래, 개인정보 또는 소비자 분쟁과 관련하여 관계 기관의 분쟁조정 절차를 이용할 수 있습니다.</li>
                </ol>
            </section>

            <section id="terms-23" class="legal-section">
                <h2>제23조 (시행일)</h2>
                <p><strong>본 약관은 2026년 8월 14일부터 시행합니다.</strong></p>
            </section>
        </div>
    </div>

    <p class="legal-project-note">
        본 약관은 ODITJI의 현재 구현 범위를 기준으로 작성된 프로젝트용 이용약관입니다.
        실제 상용 전자상거래 서비스로 운영하는 경우 운영주체 정보, 통신판매 관련 고지, 판매·중개 구조,
        배송·교환·환불 정책 등을 실제 사업 운영방식에 맞게 최종 검토해야 합니다.
    </p>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>
