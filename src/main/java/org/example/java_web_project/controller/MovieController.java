package org.example.java_web_project.controller;


import lombok.RequiredArgsConstructor;
import org.example.java_web_project.model.Movie;
import org.example.java_web_project.repository.MovieRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieRepository movieRepository;

    // Trang chủ customer
    @GetMapping
    public String home(Model model, @RequestParam(required = false) String keyword) {
        List<Movie> nowShowing;
        List<Movie> comingSoon;

        if (keyword != null && !keyword.isBlank()) {
            // Tìm kiếm theo tên
            List<Movie> results = movieRepository.findByTitleContainingIgnoreCase(keyword);
            model.addAttribute("keyword", keyword);
            model.addAttribute("searchResults", results);
            model.addAttribute("nowShowing", List.of());
            model.addAttribute("comingSoon", List.of());
        } else {
            nowShowing = movieRepository.findByStatus(Movie.Status.NOW_SHOWING);
            comingSoon = movieRepository.findByStatus(Movie.Status.COMING_SOON);
            model.addAttribute("nowShowing", nowShowing);
            model.addAttribute("comingSoon", comingSoon);
            model.addAttribute("keyword", "");
        }

        return "customer/home";
    }

    // Chi tiết phim (public)
    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phim không tồn tại"));
        model.addAttribute("movie", movie);
        return "customer/movie-detail";
    }
}