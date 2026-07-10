package com.project.oditji.goods.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import com.project.oditji.goods.dao.GoodsDAO;
import com.project.oditji.goods.vo.GoodsVO;

@Service
public class GoodsServiceImpl implements GoodsService {
    private final GoodsDAO goodsDAO;

    public GoodsServiceImpl(GoodsDAO goodsDAO) {
        this.goodsDAO = goodsDAO;
    }

    @Override
    public List<GoodsVO> searchGoods(String keyword, int page, int pageSize) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        if (normalizedKeyword.isEmpty()) {
            return new ArrayList<GoodsVO>();
        }
        int safePage = page <= 0 ? 1 : page;
        int safePageSize = pageSize <= 0 ? 12 : pageSize;
        int offset = (safePage - 1) * safePageSize;
        List<GoodsVO> result = goodsDAO.searchGoods(normalizedKeyword, offset, safePageSize);
        return result == null ? new ArrayList<GoodsVO>() : result;
    }

    @Override
    public int countSearchGoods(String keyword) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        if (normalizedKeyword.isEmpty()) {
            return 0;
        }
        return goodsDAO.countSearchGoods(normalizedKeyword);
    }
}
