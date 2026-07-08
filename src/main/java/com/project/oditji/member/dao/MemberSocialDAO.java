package com.project.oditji.member.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.project.oditji.member.vo.MemberSocialJoinVO;
import com.project.oditji.member.vo.MemberSocialVO;

@Mapper
public interface MemberSocialDAO {

    int insertMemberSocial(MemberSocialVO memberSocialVO);

    MemberSocialJoinVO selectMemberBySocial(
            @Param("provider") String provider,
            @Param("providerUserId") String providerUserId
    );
}