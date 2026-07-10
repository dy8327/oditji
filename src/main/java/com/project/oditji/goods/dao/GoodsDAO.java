package com.project.oditji.goods.dao;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.project.oditji.goods.vo.GoodsVO;

@Mapper
public interface GoodsDAO {
    List<GoodsVO> searchGoods(@Param("keyword") String keyword,
                             @Param("offset") int offset,
                             @Param("limit") int limit);
    int countSearchGoods(@Param("keyword") String keyword);
}
