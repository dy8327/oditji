package com.project.oditji.business.vo;

public class ActorSearchVO {

    private long actorNo;
    private long tmdbActorId;
    private String actorName;
    private String profilePath;

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
}