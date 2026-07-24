package com.project.oditji.member.service;

import java.util.List;

import com.project.oditji.member.vo.PlatformVO;

public interface MemberPlatformService {

    List<PlatformVO> findPlatformList();

    List<PlatformVO> findMemberPlatformList(Long memberNo);

    /**
     * 회원의 OTT 선택 정보를 저장한다.
     *
     * join.jsp의 validateOttSelection()과 동일한 정책을 따른다:
     * platformNoList와 noOtt 중 정확히 하나만 채워져 있어야 하며,
     * 둘 다 비어있거나 둘 다 채워져 있으면 예외를 던진다.
     *
     * @param noOtt "Y"면 OTT 없음을 의미한다.
     */
    void saveMemberPlatforms(Long memberNo, List<Long> platformNoList, String noOtt);

    int countMemberPlatform(Long memberNo);
}
