package com.project.oditji.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {

	@GetMapping("/contents/search")
	public String search() {
		return "contents/search";
	}

	@GetMapping("/contents/detail/{id}")
	public String detail(@PathVariable String id) {
		return "contents/detail";
	}

	@GetMapping("/mypage/favorites")
	public String favorites() {
		return "mypage/favorites";
	}

		
	@GetMapping("/admin/dashboard")
	public String dashboard() {
		return "admin/dashboard";
	}
}
