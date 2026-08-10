package com.project.oditji.event.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.common.util.PaginationUtil;
import com.project.oditji.event.dao.OttDiscountDAO;
import com.project.oditji.event.vo.OttDiscountVO;

@Service
@Transactional(readOnly = true)
public class OttDiscountServiceImpl implements OttDiscountService {

    private static final String DEFAULT_FILTER = "ALL";

    private final OttDiscountDAO ottDiscountDAO;

    public OttDiscountServiceImpl(OttDiscountDAO ottDiscountDAO) {
        this.ottDiscountDAO = ottDiscountDAO;
    }

    @Override
    public List<OttDiscountVO> getDiscountList(String platform, String category, int page, int pageSize) {
        String normalizedPlatform = normalize(platform);
        String normalizedCategory = normalize(category);
        int offset = PaginationUtil.offset(page, pageSize);

        List<OttDiscountVO> discountList =
                ottDiscountDAO.selectDiscountList(normalizedPlatform, normalizedCategory, offset, pageSize);
        return discountList == null ? List.of() : discountList;
    }

    @Override
    public int getDiscountListCount(String platform, String category) {
        String normalizedPlatform = normalize(platform);
        String normalizedCategory = normalize(category);

        return ottDiscountDAO.selectDiscountListCount(normalizedPlatform, normalizedCategory);
    }

    @Override
    public OttDiscountVO getHeroDiscount(String platform, String category) {
        String normalizedPlatform = normalize(platform);
        String normalizedCategory = normalize(category);

        return ottDiscountDAO.selectHeroDiscount(normalizedPlatform, normalizedCategory);
    }

    @Override
    public OttDiscountVO getDiscountDetail(Long discountId) {
        if (discountId == null || discountId <= 0) {
            return null;
        }
        return ottDiscountDAO.selectDiscountById(discountId);
    }

    @Override
    public List<OttDiscountVO> getAdminDiscountList(String platform, String category, String status) {
        String normalizedPlatform = normalize(platform);
        String normalizedCategory = normalize(category);
        String normalizedStatus = normalizeStatus(status);

        List<OttDiscountVO> discountList =
                ottDiscountDAO.selectAdminDiscountList(normalizedPlatform, normalizedCategory, normalizedStatus);
        return discountList == null ? List.of() : discountList;
    }

    @Override
    @Transactional
    public boolean createDiscount(OttDiscountVO discountVO) {
        return ottDiscountDAO.insertDiscount(discountVO) > 0;
    }

    @Override
    @Transactional
    public boolean updateDiscount(OttDiscountVO discountVO) {
        return ottDiscountDAO.updateDiscount(discountVO) > 0;
    }

    @Override
    @Transactional
    public boolean deleteDiscount(Long discountId) {
        return ottDiscountDAO.deleteDiscount(discountId) > 0;
    }

    @Override
    @Transactional
    public boolean activateDiscount(Long discountId) {
        if (discountId == null || discountId <= 0) {
            return false;
        }
        return ottDiscountDAO.activateDiscount(discountId) > 0;
    }

    @Override
    @Transactional
    public int deactivateExpiredDiscounts() {
        return ottDiscountDAO.updateExpiredDiscounts();
    }

    private String normalize(String value) {
        return (value == null || value.trim().isEmpty()) ? DEFAULT_FILTER : value.trim().toUpperCase();
    }

    /** 상태 필터는 'Y'/'N'만 유효한 값으로 인정하고, 그 외(빈 값 포함)는 전체 조회로 취급한다. */
    private String normalizeStatus(String value) {
        if (value == null) {
            return DEFAULT_FILTER;
        }
        String normalized = value.trim().toUpperCase();
        return ("Y".equals(normalized) || "N".equals(normalized)) ? normalized : DEFAULT_FILTER;
    }
}
