package com.project.oditji.common.vo;

/**
 * TMDB 배우 조회와 사업자 상품 배우 선택에서 공통으로 사용하는 배우 정보입니다.
 */
public abstract class ActorBaseVO {

    private Long actorNo;
    private Long tmdbActorId;
    private String actorName;
    private String profilePath;
    private String characterName;
    private Integer displayOrder;

    public Long getActorNo() {
        return actorNo;
    }

    public void setActorNo(Long actorNo) {
        this.actorNo = actorNo;
    }

    public Long getTmdbActorId() {
        return tmdbActorId;
    }

    public void setTmdbActorId(Long tmdbActorId) {
        this.tmdbActorId = tmdbActorId;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public String getProfilePath() {
        return profilePath;
    }

    public void setProfilePath(String profilePath) {
        this.profilePath = profilePath;
    }

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
