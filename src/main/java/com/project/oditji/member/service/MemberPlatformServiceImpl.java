package com.project.oditji.member.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.member.dao.MemberPlatformDAO;
import com.project.oditji.member.vo.PlatformVO;

@Service
public class MemberPlatformServiceImpl implements MemberPlatformService {

    private final MemberPlatformDAO memberPlatformDAO;

    public MemberPlatformServiceImpl(MemberPlatformDAO memberPlatformDAO) {
        this.memberPlatformDAO = memberPlatformDAO;
    }

    @Override
    public List<PlatformVO> findPlatformList() {
        return memberPlatformDAO.selectPlatformList();
    }

    @Override
    @Transactional
    public void saveMemberPlatforms(Long memberNo, List<Long> platformNoList) {
        for (Long platformNo : platformNoList) {
            if (platformNo != null) {
                memberPlatformDAO.insertMemberPlatform(memberNo, platformNo);
            }
        }
    }

    @Override
    public int countMemberPlatform(Long memberNo) {
        return memberPlatformDAO.countMemberPlatform(memberNo);
    }
}