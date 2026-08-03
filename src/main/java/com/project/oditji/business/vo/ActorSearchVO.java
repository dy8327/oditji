package com.project.oditji.business.vo;

import com.project.oditji.common.vo.ActorBaseVO;

/**
 * 사업자 상품 등록·수정 화면의 배우 검색 결과입니다.
 */
public class ActorSearchVO extends ActorBaseVO {

    @Override
    public String toString() {
        return "ActorSearchVO{" +
                "actorNo=" + getActorNo() +
                ", tmdbActorId=" + getTmdbActorId() +
                ", actorName='" + getActorName() + '\'' +
                ", profilePath='" + getProfilePath() + '\'' +
                ", characterName='" + getCharacterName() + '\'' +
                ", displayOrder=" + getDisplayOrder() +
                '}';
    }
}
