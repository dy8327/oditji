package com.project.oditji.member.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.project.oditji.member.vo.PlatformVO;

@Mapper
public interface MemberPlatformDAO {

    List<PlatformVO> selectPlatformList();

    Long insertMemberPlatform(
            @Param("memberNo") Long memberNo,
            @Param("platformNo") Long platformNo
    );

    int countMemberPlatform(@Param("memberNo") Long memberNo);
}