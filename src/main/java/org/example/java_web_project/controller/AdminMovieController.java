package org.example.java_web_project.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.java_web_project.dto.MovieDTO;
import org.example.java_web_project.model.Movie;
import org.example.java_web_project.service.MovieService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/movies")
@RequiredArgsConstructor
public class AdminMovieController {

    private final MovieService movieService;

    @GetMapping
    public String listMovies(Model model) {
        model.addAttribute("movies", movieService.getAllMovies());
        return "admin/movie/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("movieRequest", new MovieDTO());
        model.addAttribute("genres", movieService.getAllGenres());
        model.addAttribute("statuses", Movie.Status.values());
        return "admin/movie/form";
    }

    @PostMapping("/create")
    public String createMovie(@Valid @ModelAttribute("movieRequest") MovieDTO req,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("genres", movieService.getAllGenres());
            model.addAttribute("statuses", Movie.Status.values());
            return "admin/movie/form";
        }
        try {
            movieService.createMovie(req);
            redirectAttributes.addFlashAttribute("successMsg", "Thêm phim thành công!");
            return "redirect:/admin/movies";
        } catch (RuntimeException e) {
            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("genres", movieService.getAllGenres());
            model.addAttribute("statuses", Movie.Status.values());
            return "admin/movie/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Movie movie = movieService.getMovieById(id);

        // Pre-fill form từ entity
        MovieDTO req = new MovieDTO();
        req.setTitle(movie.getTitle());
        req.setDescription(movie.getDescription());
        req.setDirector(movie.getDirector());
        req.setActors(movie.getActors());
        req.setDuration(movie.getDuration());
        req.setReleaseDate(movie.getReleaseDate());
        req.setPoster(movie.getPoster());
        req.setTrailerUrl(movie.getTrailerUrl());
        req.setAgeRating(movie.getAgeRating());
        req.setStatus(movie.getStatus());
        req.setGenreIds(movie.getGenres().stream()
                .map(g -> g.getGenreId())
                .collect(java.util.stream.Collectors.toList()));

        model.addAttribute("movieRequest", req);
        model.addAttribute("movie", movie);
        model.addAttribute("genres", movieService.getAllGenres());
        model.addAttribute("statuses", Movie.Status.values());
        return "admin/movie/form";
    }

    @PostMapping("/{id}/edit")
    public String updateMovie(@PathVariable Integer id,
                              @Valid @ModelAttribute("movieRequest") MovieDTO req,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("movie", movieService.getMovieById(id));
            model.addAttribute("genres", movieService.getAllGenres());
            model.addAttribute("statuses", Movie.Status.values());
            return "admin/movie/form";
        }
        try {
            movieService.updateMovie(id, req);
            redirectAttributes.addFlashAttribute("successMsg", "Cập nhật phim thành công!");
            return "redirect:/admin/movies";
        } catch (RuntimeException e) {
            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("movie", movieService.getMovieById(id));
            model.addAttribute("genres", movieService.getAllGenres());
            model.addAttribute("statuses", Movie.Status.values());
            return "admin/movie/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteMovie(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            movieService.deleteMovie(id);
            redirectAttributes.addFlashAttribute("successMsg", "Xóa phim thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/movies";
    }
}