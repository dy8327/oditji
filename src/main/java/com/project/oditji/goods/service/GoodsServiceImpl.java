package com.project.oditji.goods.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.goods.dao.GoodsDAO;
import com.project.oditji.goods.vo.GoodsVO;

@Service
@Transactional(readOnly = true)
public class GoodsServiceImpl implements GoodsService {

    private final GoodsDAO goodsDAO;

    public GoodsServiceImpl(
            GoodsDAO goodsDAO) {

        this.goodsDAO = goodsDAO;
    }

    @Override
    public List<GoodsVO> searchGoods(
            String keyword,
            int page,
            int pageSize) {

        String normalizedKeyword =
                normalizeKeyword(keyword);

        if (normalizedKeyword.isEmpty()) {
            return new ArrayList<GoodsVO>();
        }

        int normalizedPage =
                normalizePage(page);

        int normalizedPageSize =
                normalizePageSize(pageSize);

        int startRow =
                (normalizedPage - 1)
                        * normalizedPageSize
                        + 1;

        int endRow =
                normalizedPage
                        * normalizedPageSize;

        List<GoodsVO> resultList =
                goodsDAO.selectSearchGoods(
                        normalizedKeyword,
                        startRow,
                        endRow
                );

        if (resultList == null) {
            return new ArrayList<GoodsVO>();
        }

        return resultList;
    }

    @Override
    public int countSearchGoods(
            String keyword) {

        String normalizedKeyword =
                normalizeKeyword(keyword);

        if (normalizedKeyword.isEmpty()) {
            return 0;
        }

        return goodsDAO.countSearchGoods(
                normalizedKeyword
        );
    }

    private String normalizeKeyword(
            String keyword) {

        if (keyword == null) {
            return "";
        }

        return keyword.trim();
    }

    private int normalizePage(
            int page) {

        return page <= 0
                ? 1
                : page;
    }

    private int normalizePageSize(
            int pageSize) {

        if (pageSize <= 0) {
            return 12;
        }

        if (pageSize > 100) {
            return 100;
        }

        return pageSize;
    }
}