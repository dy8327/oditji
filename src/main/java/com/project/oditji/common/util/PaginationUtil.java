package com.project.oditji.common.util;

import com.project.oditji.common.vo.PageVO;

/**
 * 관리자 화면 목록 페이징 계산 공용 유틸.
 *
 * PageVO는 값만 담는 순수 VO라서(계산 로직 없음), 기존에는 AdminController의
 * buildMemberPagination()에서 회원 목록에 대해서만 페이지 번호/블록 계산을 했다.
 * 같은 계산 로직을 리뷰/이벤트/상품/주문/사업자/정산 등 다른 관리자 목록 화면에도
 * 동일하게 적용하기 위해 공용 메서드로 분리했다.
 */
public class PaginationUtil {

    private PaginationUtil() {
    }

    /**
     * @param requestedPage 사용자가 요청한 페이지 번호 (1 미만/총 페이지 초과 시 보정됨)
     * @param totalCount    검색 조건에 맞는 전체 건수
     * @param pageSize      한 페이지에 보여줄 건수
     * @param blockSize     페이지 번호를 한 번에 몇 개씩 끊어서 보여줄지 (예: 5 -> 1~5, 6~10 ...)
     */
    public static PageVO build(int requestedPage, int totalCount, int pageSize, int blockSize) {

        int totalPage = (int) Math.ceil((double) totalCount / pageSize);
        if (totalPage < 1) {
            totalPage = 1;
        }

        int currentPage = requestedPage;
        if (currentPage < 1) {
            currentPage = 1;
        } else if (currentPage > totalPage) {
            currentPage = totalPage;
        }

        int startPage = ((currentPage - 1) / blockSize) * blockSize + 1;
        int endPage = Math.min(startPage + blockSize - 1, totalPage);

        PageVO pageVO = new PageVO();
        pageVO.setCurrentPage(currentPage);
        pageVO.setPageSize(pageSize);
        pageVO.setTotalCount(totalCount);
        pageVO.setTotalPage(totalPage);
        pageVO.setStartPage(startPage);
        pageVO.setEndPage(endPage);
        pageVO.setPrev(startPage > 1);
        pageVO.setNext(endPage < totalPage);

        return pageVO;
    }

    /** 특정 페이지 조회 시 DB에 넘길 OFFSET(건너뛸 행 수)을 계산한다. */
    public static int offset(int currentPage, int pageSize) {
        int page = (currentPage < 1) ? 1 : currentPage;
        return (page - 1) * pageSize;
    }
}
