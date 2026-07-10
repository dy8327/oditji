package com.project.oditji.content.vo;

import java.util.ArrayList;
import java.util.List;

public class PersonFilmographyVO {

    private Long tmdbPersonId;
    private String personName;
    private String profilePath;
    private String biography;
    private String birthday;
    private String placeOfBirth;
    private String role;

    private List<FilmographyVO> castList =
            new ArrayList<FilmographyVO>();

    private List<FilmographyVO> directorList =
            new ArrayList<FilmographyVO>();

    private List<FilmographyVO> productionList =
            new ArrayList<FilmographyVO>();

    public PersonFilmographyVO() {
    }

    public Long getTmdbPersonId() {
        return tmdbPersonId;
    }

    public void setTmdbPersonId(Long tmdbPersonId) {
        this.tmdbPersonId = tmdbPersonId;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getProfilePath() {
        return profilePath;
    }

    public void setProfilePath(String profilePath) {
        this.profilePath = profilePath;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public void setPlaceOfBirth(String placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<FilmographyVO> getCastList() {
        return castList;
    }

    public void setCastList(List<FilmographyVO> castList) {
        this.castList = castList == null
                ? new ArrayList<FilmographyVO>()
                : castList;
    }

    public List<FilmographyVO> getDirectorList() {
        return directorList;
    }

    public void setDirectorList(List<FilmographyVO> directorList) {
        this.directorList = directorList == null
                ? new ArrayList<FilmographyVO>()
                : directorList;
    }

    public List<FilmographyVO> getProductionList() {
        return productionList;
    }

    public void setProductionList(List<FilmographyVO> productionList) {
        this.productionList = productionList == null
                ? new ArrayList<FilmographyVO>()
                : productionList;
    }

    public int getTotalCount() {
        return castList.size()
                + directorList.size()
                + productionList.size();
    }
}
