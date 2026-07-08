package com.project.oditji.verify.dao;

import com.project.oditji.verify.vo.IdentityVerifyLogVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VerifyDAO {

    int insertVerifyLog(IdentityVerifyLogVO logVO);
}