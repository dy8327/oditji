package com.project.oditji.member.support;

import java.util.Date;

/**
 * 탈퇴 회원 복구 정책 관련 공통 상수/유틸.
 *
 * - 마이페이지 자진 탈퇴 시 STATUS = 'WITHDRAWN', WITHDRAWN_AT = SYSDATE 로 소프트 삭제된다.
 * - WITHDRAWN_AT 기준 RESTORE_PERIOD_DAYS(7일) 이내라면 로그인 시도 시 복구할 수 있다.
 * - 7일이 지나면 MemberDeleteScheduler에 의해 실제 데이터가 삭제된다.
 *
 * MemberController(일반 로그인)와 KakaoLoginController(SNS 로그인)에서
 * 동일한 정책/문구를 사용하기 위해 공통으로 둔다.
 */
public final class WithdrawPolicy {

    public static final int RESTORE_PERIOD_DAYS = 7;

    private WithdrawPolicy() {
    }

    /**
     * 탈퇴 시각(withdrawnAt) 기준 남은 복구 가능 일수를 계산한다.
     * 정보가 없으면 0을 반환한다.
     */
    public static int calcDaysLeft(Date withdrawnAt) {

        if (withdrawnAt == null) {
            return 0;
        }

        long diffMillis = System.currentTimeMillis() - withdrawnAt.getTime();
        long diffDays = diffMillis / (1000L * 60 * 60 * 24);

        int daysLeft = (int) (RESTORE_PERIOD_DAYS - diffDays);

        return Math.max(daysLeft, 0);
    }

    /**
     * 로그인 화면에 노출할 탈퇴 안내 메시지를 만든다.
     */
    public static String buildWithdrawnMessage(Date withdrawnAt) {

        int daysLeft = calcDaysLeft(withdrawnAt);

        if (daysLeft <= 0) {
            return "탈퇴한 계정입니다. 복구 가능 기한이 지났습니다.";
        }

        return "탈퇴한 계정입니다. (복구 가능 기한: " + daysLeft + "일 남음)";
    }
}
