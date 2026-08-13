package com.project.oditji.tmdb.vo;

public class DirectorVO {

    private Integer directorNo;
    private Long tmdbDirectorId;
    private String directorName;
    private String profilePath;

    private String directorType;
    private Integer displayOrder;

    public Integer getDirectorNo() {
        return directorNo;
    }

    public void setDirectorNo(Integer directorNo) {
        this.directorNo = directorNo;
    }

    public Long getTmdbDirectorId() {
        return tmdbDirectorId;
    }

    public void setTmdbDirectorId(Long tmdbDirectorId) {
        this.tmdbDirectorId = tmdbDirectorId;
    }

    public String getDirectorName() {
        return directorName;
    }

    public void setDirectorName(String directorName) {
        this.directorName = directorName;
    }

    public String getProfilePath() {
        return profilePath;
    }

    public void setProfilePath(String profilePath) {
        this.profilePath = profilePath;
    }

    public String getDirectorType() {
        return directorType;
    }

    public void setDirectorType(String directorType) {
        this.directorType = directorType;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}