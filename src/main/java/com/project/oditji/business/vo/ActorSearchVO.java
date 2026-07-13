package com.project.oditji.business.vo;

public class ActorSearchVO {

    private long actorNo;
    private long tmdbActorId;
    private String actorName;
    private String profilePath;

    // CONTENT_ACTOR 테이블
    private String characterName;
    private Integer displayOrder;

    public ActorSearchVO() {
    }

    public long getActorNo() {
        return actorNo;
    }

    public void setActorNo(long actorNo) {
        this.actorNo = actorNo;
    }

    public long getTmdbActorId() {
        return tmdbActorId;
    }

    public void setTmdbActorId(long tmdbActorId) {
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

    @Override
    public String toString() {
        return "ActorSearchVO{" +
                "actorNo=" + actorNo +
                ", tmdbActorId=" + tmdbActorId +
                ", actorName='" + actorName + '\'' +
                ", profilePath='" + profilePath + '\'' +
                ", characterName='" + characterName + '\'' +
                ", displayOrder=" + displayOrder +
                '}';
    }
}