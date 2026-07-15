package com.project.oditji.member.exception;

/**
 * 정지(BLOCKED) 상태의 회원이 로그인을 시도할 때 발생하는 예외.
 *
 * 정지 회원은 복구 절차 없이 단순 안내만 노출한다.
 * (관리자에 의한 정지 해제만 가능)
 */
public class MemberBlockedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MemberBlockedException(String message) {
        super(message);
    }
}
