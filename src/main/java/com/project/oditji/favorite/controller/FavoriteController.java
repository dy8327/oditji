package com.project.oditji.favorite.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/favorite")
public class FavoriteController {

    @GetMapping("/list")
    public String favoriteList() {

        return "favorite/favoriteList";
    }

}