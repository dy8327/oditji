package com.project.oditji.tmdb.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.oditji.tmdb.dao.TmdbDAO;

@Service
public class TmdbServiceImpl implements TmdbService {

    @Autowired
    private TmdbDAO tmdbDAO;

    @Override
    public int loadMovieData() {

        System.out.println("영화 데이터 적재 시작");

        return 0;
    }

    @Override
    public int loadTvData() {

        System.out.println("TV 데이터 적재 시작");

        return 0;
    }

    @Override
    public int loadAllData() {

        int movieCount = loadMovieData();
        int tvCount = loadTvData();

        return movieCount + tvCount;
    }

}