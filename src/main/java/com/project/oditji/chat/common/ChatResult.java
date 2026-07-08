package com.project.oditji.chat.common;

public class ChatResult {

    /**
     * 성공
     */
    public static final int SUCCESS = 1;

    /**
     * 실패
     */
    public static final int FAIL = 0;

    /**
     * 이미 참가
     */
    public static final int ALREADY_JOINED = 2;

    /**
     * 정원 초과
     */
    public static final int ROOM_FULL = 3;

    /**
     * 존재하지 않는 방
     */
    public static final int ROOM_NOT_FOUND = 4;

}