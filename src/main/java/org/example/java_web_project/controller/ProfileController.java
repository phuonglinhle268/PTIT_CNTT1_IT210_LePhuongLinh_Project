package org.example.java_web_project.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.java_web_project.dto.ProfileDTO;
import org.example.java_web_project.dto.SessionUser;
import org.example.java_web_project.model.UserProfile;
import org.example.java_web_project.service.AuthService;
import org.example.java_web_project.service.ProfileService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public String viewProfile(HttpSession session, Model model) {
        SessionUser sessionUser = (SessionUser) session.getAttribute(AuthService.SESSION_KEY);
        model.addAttribute("user", profileService.getUser(sessionUser.getUserId()));
        model.addAttribute("profile", profileService.getProfile(sessionUser.getUserId()));
        return "profile/view";
    }

    @GetMapping("/edit")
    public String showEditForm(HttpSession session, Model model) {
        SessionUser sessionUser = (SessionUser) session.getAttribute(AuthService.SESSION_KEY);
        UserProfile profile = profileService.getProfile(sessionUser.getUserId());

        // Pre-fill form từ entity hiện tại
        ProfileDTO req = new ProfileDTO();
        req.setFullname(profile.getFullname());
        req.setPhone(profile.getPhone());
        req.setGender(profile.getGender());
        req.setAddress(profile.getAddress());

        model.addAttribute("profile", profile);
        model.addAttribute("profileRequest", req);
        return "profile/edit";
    }

    @PostMapping("/edit")
    public String updateProfile(@Valid @ModelAttribute("profileRequest") ProfileDTO req,
                                BindingResult bindingResult,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            SessionUser sessionUser = (SessionUser) session.getAttribute(AuthService.SESSION_KEY);
            model.addAttribute("profile", profileService.getProfile(sessionUser.getUserId()));
            return "profile/edit";
        }
        try {
            SessionUser sessionUser = (SessionUser) session.getAttribute(AuthService.SESSION_KEY);
            profileService.updateProfile(sessionUser.getUserId(), req);

            // Cập nhật fullname trong session để header hiển thị đúng
            sessionUser.setFullname(req.getFullname());
            session.setAttribute(AuthService.SESSION_KEY, sessionUser);

            redirectAttributes.addFlashAttribute("successMsg", "Cập nhật hồ sơ thành công!");
            return "redirect:/profile";
        } catch (RuntimeException e) {
            model.addAttribute("errorMsg", e.getMessage());
            return "profile/edit";
        }
    }

    @PostMapping("/avatar")
    public String uploadAvatar(@RequestParam("file") MultipartFile file,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        SessionUser sessionUser = (SessionUser) session.getAttribute(AuthService.SESSION_KEY);
        try {
            String avatarUrl = profileService.uploadAvatar(sessionUser.getUserId(), file);
            sessionUser.setAvatar(avatarUrl);
            session.setAttribute(AuthService.SESSION_KEY, sessionUser);
            redirectAttributes.addFlashAttribute("successMsg", "Cập nhật ảnh thành công!");
        } catch (RuntimeException | IOException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/profile";
    }
}
