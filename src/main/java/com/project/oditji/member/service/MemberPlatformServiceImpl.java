package com.project.oditji.member.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.member.dao.MemberPlatformDAO;
import com.project.oditji.member.vo.PlatformVO;

@Service
public class MemberPlatformServiceImpl implements MemberPlatformService {

    private final MemberPlatformDAO memberPlatformDAO;

    public MemberPlatformServiceImpl(
            MemberPlatformDAO memberPlatformDAO) {

        this.memberPlatformDAO = memberPlatformDAO;
    }

    @Override
    public List<PlatformVO> findPlatformList() {

        List<PlatformVO> platformList =
                memberPlatformDAO.findPlatformList();

        return platformList == null
                ? new ArrayList<PlatformVO>()
                : platformList;
    }

    @Override
    public List<PlatformVO> findMemberPlatformList(
            Long memberNo) {

        if (memberNo == null) {
            return new ArrayList<PlatformVO>();
        }

        List<PlatformVO> platformList =
                memberPlatformDAO.selectMemberSelectedPlatformList(
                        memberNo
                );

        return platformList == null
                ? new ArrayList<PlatformVO>()
                : platformList;
    }

    @Override
    @Transactional
    public void saveMemberPlatforms(
            Long memberNo,
            List<Long> platformNoList) {

        if (memberNo == null) {
            throw new IllegalArgumentException(
                    "로그인 정보가 없습니다."
            );
        }

        if (platformNoList == null
                || platformNoList.isEmpty()) {
            throw new IllegalArgumentException(
                    "이용 중인 OTT를 하나 이상 선택해주세요."
            );
        }

        memberPlatformDAO.deleteMemberPlatforms(
                memberNo
        );

        for (Long platformNo : platformNoList) {

            if (platformNo != null) {
                memberPlatformDAO.insertMemberPlatform(
                        memberNo,
                        platformNo
                );
            }
        }
    }

    @Override
    public int countMemberPlatform(
            Long memberNo) {

        if (memberNo == null) {
            return 0;
        }

        return memberPlatformDAO.countMemberPlatform(
                memberNo
        );
    }
}
