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
            List<Long> platformNoList,
            String noOtt) {

        if (memberNo == null) {
            throw new IllegalArgumentException(
                    "로그인 정보가 없습니다."
            );
        }

        boolean hasPlatform = platformNoList != null
                && !platformNoList.isEmpty();
        boolean selectedNoOtt = "Y".equalsIgnoreCase(noOtt);

        /*
         * =========================================================
         * OTT 선택 검증 (join.jsp의 validateOttSelection()과 동일한 정책)
         *
         * 실제 OTT를 1개 이상 선택하거나 OTT 없음을 선택해야 하며,
         * 둘 다 선택된 경우는 허용하지 않는다.
         * =========================================================
         */
        if (!hasPlatform && !selectedNoOtt) {
            throw new IllegalArgumentException(
                    "이용 중인 OTT를 선택하거나 'OTT 없음'을 선택해주세요."
            );
        }

        if (hasPlatform && selectedNoOtt) {
            throw new IllegalArgumentException(
                    "OTT 없음과 다른 OTT는 동시에 선택할 수 없습니다."
            );
        }

        memberPlatformDAO.deleteMemberPlatforms(
                memberNo
        );

        /*
         * OTT 없음이 선택된 경우 기존 선택을 비우기만 하고
         * MEMBER_PLATFORM에는 별도 데이터를 저장하지 않는다.
         */
        if (!hasPlatform) {
            return;
        }

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
