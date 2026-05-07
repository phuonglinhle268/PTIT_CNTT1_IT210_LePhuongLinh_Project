package org.example.java_web_project.controller;


import lombok.RequiredArgsConstructor;
import org.example.java_web_project.model.User;
import org.example.java_web_project.repository.MovieRepository;
import org.example.java_web_project.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final MovieRepository movieRepository;
    private final UserRepository userRepository;

    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("totalMovies",   movieRepository.count());
        model.addAttribute("totalCustomers",
                userRepository.countByRole(User.Role.CUSTOMER));

        model.addAttribute("totalBookings", 0);
        model.addAttribute("totalRevenue",  0);

        return "admin/dashboard";
    }
}