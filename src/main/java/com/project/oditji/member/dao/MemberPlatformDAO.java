package com.project.oditji.member.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.project.oditji.member.vo.MemberPlatformVO;
import com.project.oditji.member.vo.PlatformVO;

@Mapper
public interface MemberPlatformDAO {

    List<PlatformVO> findPlatformList();

    int countMemberPlatform(Long memberNo);

    List<MemberPlatformVO> selectMemberPlatformList(Long memberNo);

    int insertMemberPlatform(@Param("memberNo") Long memberNo,
                             @Param("platformNo") Long platformNo);

    int deleteMemberPlatforms(@Param("memberNo") Long memberNo);
}