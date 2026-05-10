package org.example.java_web_project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.java_web_project.dto.ShowtimeDTO;
import org.example.java_web_project.model.Movie;
import org.example.java_web_project.model.Showtime;
import org.example.java_web_project.service.MovieService;
import org.example.java_web_project.service.ShowtimeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/showtimes")
@RequiredArgsConstructor
public class AdminShowtimeController {

    private final ShowtimeService showtimeService;
    private final MovieService    movieService;

    // ── Danh sách ────────────────────────────────────────────────────────────

    @GetMapping
    public String list(Model model) {
        model.addAttribute("showtimes", showtimeService.getAllShowtimes());
        return "admin/showtime/list";
    }

    // ── Tạo mới ──────────────────────────────────────────────────────────────

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("showtimeRequest", new ShowtimeDTO());
        fillFormModel(model);
        return "admin/showtime/form";
    }

    @PostMapping("/create")
    public String createShowtime(@Valid @ModelAttribute("showtimeRequest") ShowtimeDTO dto,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            fillFormModel(model);
            return "admin/showtime/form";
        }
        try {
            showtimeService.createShowtime(dto);
            ra.addFlashAttribute("successMsg", "Tạo suất chiếu thành công!");
            return "redirect:/admin/showtimes";
        } catch (RuntimeException e) {
            model.addAttribute("errorMsg", e.getMessage());
            fillFormModel(model);
            return "admin/showtime/form";
        }
    }

    // ── Chỉnh sửa ────────────────────────────────────────────────────────────

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Showtime showtime = showtimeService.getShowtimeById(id);

        // Không cho edit suất đã kết thúc / bị hủy
        if (showtime.getStatus() == Showtime.Status.ENDED
                || showtime.getStatus() == Showtime.Status.CANCELLED) {
            model.addAttribute("errorMsg", "Không thể chỉnh sửa suất chiếu đã kết thúc hoặc bị hủy.");
            model.addAttribute("showtimes", showtimeService.getAllShowtimes());
            return "admin/showtime/list";
        }

        // Pre-fill form từ entity
        ShowtimeDTO dto = new ShowtimeDTO();
        dto.setMovieId(showtime.getMovie().getMovieId());
        dto.setRoomId(showtime.getRoom().getRoomId());
        dto.setStartTime(showtime.getStartTime());
        dto.setBasePrice(showtime.getBasePrice());

        model.addAttribute("showtimeRequest", dto);
        model.addAttribute("showtime", showtime); // để biết đây là edit mode
        fillFormModel(model);
        return "admin/showtime/form";
    }

    @PostMapping("/{id}/edit")
    public String updateShowtime(@PathVariable Integer id,
                                 @Valid @ModelAttribute("showtimeRequest") ShowtimeDTO dto,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("showtime", showtimeService.getShowtimeById(id));
            fillFormModel(model);
            return "admin/showtime/form";
        }
        try {
            showtimeService.updateShowtime(id, dto);
            ra.addFlashAttribute("successMsg", "Cập nhật suất chiếu thành công!");
            return "redirect:/admin/showtimes";
        } catch (RuntimeException e) {
            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("showtime", showtimeService.getShowtimeById(id));
            fillFormModel(model);
            return "admin/showtime/form";
        }
    }

    // ── Hủy suất chiếu ───────────────────────────────────────────────────────

    @PostMapping("/{id}/delete")
    public String deleteShowtime(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            showtimeService.deleteShowtime(id);
            ra.addFlashAttribute("successMsg", "Đã hủy suất chiếu.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/showtimes";
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private void fillFormModel(Model model) {
        model.addAttribute("movies", movieService.getAllMovies().stream()
                .filter(m -> m.getStatus() != Movie.Status.STOPPED)
                .toList());
        model.addAttribute("rooms", showtimeService.getAllRooms());
    }
}