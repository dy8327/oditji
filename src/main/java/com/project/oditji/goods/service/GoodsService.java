package com.project.oditji.goods.service;

import java.util.List;
import com.project.oditji.goods.vo.GoodsVO;

public interface GoodsService {
    List<GoodsVO> searchGoods(String keyword, int page, int pageSize);
    int countSearchGoods(String keyword);
}
