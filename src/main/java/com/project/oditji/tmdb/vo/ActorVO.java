package com.project.oditji.tmdb.vo;

public class ActorVO {

    private Integer actorNo;
    private Long tmdbActorId;
    private String actorName;
    private String profilePath;

    private String characterName;
    private Integer displayOrder;

    public ActorVO() {
    }

    public Integer getActorNo() {
        return actorNo;
    }

    public void setActorNo(Integer actorNo) {
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