package com.project.oditji.content.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * 인물 필모그래피 VO의 목록 null 방어와 전체 개수 계산을 검증합니다.
 */
class PersonFilmographyVOCoverageTest {

    @Test
    void scalarFieldsAndFilmographyListsShouldRoundTrip() {
        PersonFilmographyVO person = new PersonFilmographyVO();

        person.setTmdbPersonId(100L);
        person.setPersonName("배우");
        person.setProfilePath("/profile.jpg");
        person.setBiography("소개");
        person.setBirthday("2000-01-01");
        person.setPlaceOfBirth("Seoul");
        person.setRole("ACTOR");

        FilmographyVO cast = new FilmographyVO();
        FilmographyVO director = new FilmographyVO();
        FilmographyVO production = new FilmographyVO();

        List<FilmographyVO> castList = List.of(cast);
        List<FilmographyVO> directorList = List.of(director);
        List<FilmographyVO> productionList = List.of(production);

        person.setCastList(castList);
        person.setDirectorList(directorList);
        person.setProductionList(productionList);

        assertEquals(100L, person.getTmdbPersonId());
        assertEquals("배우", person.getPersonName());
        assertEquals("/profile.jpg", person.getProfilePath());
        assertEquals("소개", person.getBiography());
        assertEquals("2000-01-01", person.getBirthday());
        assertEquals("Seoul", person.getPlaceOfBirth());
        assertEquals("ACTOR", person.getRole());

        assertSame(castList, person.getCastList());
        assertSame(directorList, person.getDirectorList());
        assertSame(productionList, person.getProductionList());
        assertEquals(3, person.getTotalCount());
    }

    @Test
    void nullListsShouldBecomeEmptyAndKeepTotalCountSafe() {
        PersonFilmographyVO person = new PersonFilmographyVO();

        person.setCastList(null);
        person.setDirectorList(null);
        person.setProductionList(null);

        assertTrue(person.getCastList().isEmpty());
        assertTrue(person.getDirectorList().isEmpty());
        assertTrue(person.getProductionList().isEmpty());
        assertEquals(0, person.getTotalCount());
    }
}
