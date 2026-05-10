package org.example.java_web_project.controller;

import lombok.RequiredArgsConstructor;
import org.example.java_web_project.model.Booking;
import org.example.java_web_project.service.BookingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffDashboardController {

    private final BookingService bookingService;

    @GetMapping("/dashboard")
    public String dashboard() {
        return "staff/dashboard";
    }

    @GetMapping("/lookup")
    public String lookup(@RequestParam(required = false) String bookingCode,
                         Model model) {
        if (bookingCode != null && !bookingCode.isBlank()) {
            try {
                Booking booking = bookingService.findByBookingCode(bookingCode.trim().toUpperCase());
                model.addAttribute("booking", booking);
            } catch (RuntimeException e) {
                model.addAttribute("errorMsg", e.getMessage());
            }
            model.addAttribute("bookingCode", bookingCode);
        }
        return "staff/lookup";
    }
}