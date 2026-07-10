package com.project.oditji.business.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.project.oditji.business.vo.ActorSearchVO;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.business.vo.ContentSearchVO;
import com.project.oditji.business.vo.GoodsManageVO;

public interface BusinessService {

    BusinessVO getBusinessByMemberNo(long memberNo);

    List<ContentSearchVO> searchContentList(String keyword);

    List<ActorSearchVO> getActorList();

    List<GoodsManageVO> getProductList(long businessNo);

    long registerProduct(GoodsManageVO goodsManageVO, MultipartFile productImage);
}