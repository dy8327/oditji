package com.project.oditji.member.service;

import java.util.List;

import com.project.oditji.member.vo.PlatformVO;

public interface MemberPlatformService {

    List<PlatformVO> findPlatformList();

    List<PlatformVO> findMemberPlatformList(Long memberNo);

    void saveMemberPlatforms(Long memberNo, List<Long> platformNoList);

    int countMemberPlatform(Long memberNo);
}
