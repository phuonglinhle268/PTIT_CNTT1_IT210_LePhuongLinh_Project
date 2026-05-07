package org.example.java_web_project.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.java_web_project.dto.LoginDTO;
import org.example.java_web_project.dto.RegisterDTO;
import org.example.java_web_project.dto.SessionUser;
import org.example.java_web_project.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/register")
    public String showRegister(HttpSession session, Model model) {
        SessionUser sessionUser = (SessionUser) session.getAttribute(AuthService.SESSION_KEY);
        if (sessionUser != null) {
            return redirectByRole(sessionUser.getRole());
        }
        model.addAttribute("registerRequest", new RegisterDTO());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterDTO req,
                           BindingResult bindingResult,
                           HttpSession session,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        SessionUser sessionUser = (SessionUser) session.getAttribute(AuthService.SESSION_KEY);
        if (sessionUser != null) {
            return redirectByRole(sessionUser.getRole());
        }
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        try {
            authService.register(req);
            redirectAttributes.addFlashAttribute("successMsg", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/auth/login";
        } catch (RuntimeException e) {
            model.addAttribute("errorMsg", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/login")
    public String showLogin(HttpSession session, Model model) {
        SessionUser sessionUser = (SessionUser) session.getAttribute(AuthService.SESSION_KEY);
        if (sessionUser != null) {
            return redirectByRole(sessionUser.getRole());
        }
        model.addAttribute("loginRequest", new LoginDTO());
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginRequest") LoginDTO req,
                        BindingResult bindingResult,
                        HttpSession session,
                        Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/login";
        }
        try {
            SessionUser sessionUser = authService.login(req, session);
            return redirectByRole(sessionUser.getRole());
        } catch (RuntimeException e) {
            model.addAttribute("errorMsg", e.getMessage());
            return "auth/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes ra) {
        authService.logout(session);
        ra.addFlashAttribute("successMsg", "Đã đăng xuất thành công.");
        return "redirect:/auth/login";
    }

    private String redirectByRole(String role) {
        if ("ADMIN".equals(role)) return "redirect:/admin/dashboard";
        if ("STAFF".equals(role)) return "redirect:/staff/dashboard";
        return "redirect:/movies";
    }
}