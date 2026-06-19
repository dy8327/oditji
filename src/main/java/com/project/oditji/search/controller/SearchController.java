package com.project.oditji.search.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class SearchController {

    @Value("${tmdb.api.base-url}")
    private String baseUrl;

    @Value("${tmdb.api.token}")
    private String token;

    @Value("${tmdb.api.image-base-url}")
    private String imageBaseUrl;

    @Value("${tmdb.api.language}")
    private String language;

    @Value("${tmdb.api.region}")
    private String region;

    @GetMapping("/search")
    public String searchResult(@RequestParam(value = "keyword", required = false) String keyword, 
                               HttpServletRequest request, 
                               Model model) {
        
        try {
            String apiUrl = "";
            
            // 🛠️ 검색창에 입력값이 없을 때 (null이거나 공백일 때)
            if (keyword == null || keyword.trim().isEmpty()) {
                // TMDB 트렌드 API를 호출하여 일주일간 가장 인기 있는 영화/시리즈 목록을 '모두' 가져옵니다.
                apiUrl = baseUrl + "/trending/all/week?language=" + language;
                model.addAttribute("searchTitle", "지금 인기 있는 콘텐츠"); // UI 표시용 제목
            } 
            // 🛠️ 사용자가 검색어를 입력했을 때
            else {
                String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
                apiUrl = baseUrl + "/search/multi?query=" + encodedKeyword 
                                + "&language=" + language 
                                + "&region=" + region 
                                + "&page=1";
                model.addAttribute("searchTitle", "'" + keyword + "' 검색 결과"); // UI 표시용 제목
            }

            URL url = URI.create(apiUrl).toURL(); 
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                String inputLine;
                StringBuilder resJson = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    resJson.append(inputLine);
                }
                in.close();

                JSONObject jsonObj = new JSONObject(resJson.toString());
                JSONArray results = jsonObj.getJSONArray("results");

                model.addAttribute("searchResults", results.toList());
                model.addAttribute("totalResults", jsonObj.getInt("total_results"));
            } else {
                model.addAttribute("totalResults", 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("totalResults", 0);
        }

        // 뷰단 유지를 위해 입력받은 keyword 상태 전달 (비어있으면 빈 문자열)
        model.addAttribute("keyword", keyword != null ? keyword.trim() : "");
        model.addAttribute("imageBaseUrl", imageBaseUrl);
        
        return "search/searchResult";
    }
}