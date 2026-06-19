package com.project.oditji.tmdb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.project.oditji.tmdb.service.TmdbService;

@Controller
public class TmdbController {

    @Autowired
    private TmdbService tmdbService;

    @GetMapping("/admin/tmdb")
    public String tmdbPage() {

        return "admin/tmdb";
    }

    @PostMapping("/admin/tmdb/movie")
    public String loadMovie() {

        tmdbService.loadMovieData();

        return "redirect:/admin/tmdb";
    }

    @PostMapping("/admin/tmdb/tv")
    public String loadTv() {

        tmdbService.loadTvData();

        return "redirect:/admin/tmdb";
    }

    @PostMapping("/admin/tmdb/all")
    public String loadAll() {

        tmdbService.loadAllData();

        return "redirect:/admin/tmdb";
    }

}