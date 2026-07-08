package com.project.oditji.tmdb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.project.oditji.tmdb.service.TmdbService;

@Controller
public class TmdbController {

    private final TmdbService tmdbService;

    TmdbController(TmdbService tmdbService) {
        this.tmdbService = tmdbService;
    }

    @GetMapping("/admin/tmdb")
    public String tmdbPage() {

        return "admin/tmdb";
    }

    @PostMapping("/admin/tmdb/movie")
    public String loadMovie() {

        tmdbService.loadMovieData();

        return "redirect:/admin/tmdb";
    }

    @PostMapping("/admin/tmdb/movie/update")
    public String updateMovieDetail() {

        tmdbService.updateMovieDetailData();

        return "redirect:/admin/tmdb";
    }

    @PostMapping("/admin/tmdb/movie/platform")
    public String loadMoviePlatform() {

        tmdbService.loadMoviePlatformData();

        return "redirect:/admin/tmdb";
    }

    @PostMapping("/admin/tmdb/movie/full")
    public String loadMovieFull() {

        tmdbService.loadMovieFullData();

        return "redirect:/admin/tmdb";
    }

    @PostMapping("/admin/tmdb/tv")
    public String loadTv() {

        tmdbService.loadTvData();

        return "redirect:/admin/tmdb";
    }

    @PostMapping("/admin/tmdb/tv/update")
    public String updateTvDetail() {

        tmdbService.updateTvDetailData();

        return "redirect:/admin/tmdb";
    }

    @PostMapping("/admin/tmdb/tv/platform")
    public String loadTvPlatform() {

        tmdbService.loadTvPlatformData();

        return "redirect:/admin/tmdb";
    }

    @PostMapping("/admin/tmdb/tv/full")
    public String loadTvFull() {

        tmdbService.loadTvFullData();

        return "redirect:/admin/tmdb";
    }

    @PostMapping("/admin/tmdb/all")
    public String loadAll() {

        tmdbService.loadAllData();

        return "redirect:/admin/tmdb";
    }
}