package com.project.oditji.holiday.service;

import java.util.Map;

/**
 * 한국천문연구원(공공데이터포털) 특일 정보제공 서비스의
 * "공휴일 정보 조회(getRestDeInfo)" 오퍼레이션을 사용해
 * 연·월 기준 공휴일 정보를 제공합니다.
 */
public interface HolidayService {

    /**
     * 지정한 연·월의 공휴일 목록을 조회합니다.
     *
     * @param year  조회할 연도 (예: 2026)
     * @param month 조회할 월 (1~12)
     * @return key는 해당 월의 "일(day)", value는 공휴일 명칭(예: "개천절")인 맵.
     *         공휴일이 없거나 API 조회에 실패하면 빈 맵을 반환합니다.
     */
    Map<Integer, String> getHolidaysByMonth(int year, int month);
}
