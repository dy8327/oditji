package com.project.oditji.member.exception;

import java.time.LocalDateTime;

/**
 * 탈퇴(WITHDRAWN) 상태의 회원이 로그인을 시도할 때 발생하는 예외.
 *
 * 일반 로그인은 ID/PW 매칭에 성공한 뒤에 이 예외가 발생하고,
 * 카카오 로그인은 OAuth 토큰 발급/사용자 조회에 성공한 뒤에 발생하므로
 * 이 시점에는 이미 "본인 인증"이 끝난 상태이다.
 *
 * 따라서 복구 처리 시 비밀번호를 재입력받지 않고,
 * 세션에 memberNo를 저장해두는 방식으로 복구를 허용한다.
 */
public class MemberWithdrawnException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Long memberNo;
    private final LocalDateTime withdrawnAt;

    public MemberWithdrawnException(String message, Long memberNo, LocalDateTime withdrawnAt) {
        super(message);
        this.memberNo = memberNo;
        this.withdrawnAt = withdrawnAt;
    }

    public Long getMemberNo() {
        return memberNo;
    }

    public LocalDateTime getWithdrawnAt() {
        return withdrawnAt;
    }
}
