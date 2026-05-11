package org.example.java_web_project.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.java_web_project.dto.BookingDTO;
import org.example.java_web_project.dto.SessionUser;
import org.example.java_web_project.model.Booking;
import org.example.java_web_project.model.Showtime;
import org.example.java_web_project.model.User;
import org.example.java_web_project.repository.UserRepository;
import org.example.java_web_project.service.AuthService;
import org.example.java_web_project.service.BookingService;
import org.example.java_web_project.service.ShowtimeService;
import org.example.java_web_project.service.VNPayService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CustomerBookingController {

    private final ShowtimeService showtimeService;
    private final BookingService  bookingService;
    private final VNPayService    vnPayService;
    private final UserRepository  userRepository;

    //Danh sách suất chiếu của 1 phim

    @GetMapping("/customer/showtimes/{movieId}")
    public String showtimeList(@PathVariable Integer movieId, Model model) {
        model.addAttribute("showtimes",
                showtimeService.getAvailableShowtimesByMovie(movieId));
        return "customer/showtime-list";
    }

    //Trang chọn ghế

    @GetMapping("/customer/booking/{showtimeId}")
    public String seatSelect(@PathVariable Integer showtimeId,
                             RedirectAttributes ra, Model model) {
        Showtime showtime = showtimeService.getShowtimeById(showtimeId);

        if (showtime.getStatus() == Showtime.Status.ENDED
                || showtime.getStatus() == Showtime.Status.CANCELLED) {
            ra.addFlashAttribute("errorMsg", "Suất chiếu này đã kết thúc hoặc bị hủy.");
            return "redirect:/movies";
        }

        model.addAttribute("showtime",  showtime);
        model.addAttribute("seatMap",   showtimeService.getSeatMap(showtimeId));
        model.addAttribute("soldOut",   showtime.getStatus() == Showtime.Status.SOLD_OUT);
        return "customer/seat-select";
    }

    //Xác nhận đặt vé → tạo đơn PENDING → redirect VNPay

    @PostMapping("/customer/booking/confirm")
    public String confirmBooking(@RequestParam Integer showtimeId,
                                 @RequestParam(required = false) List<Integer> seatIds,
                                 @RequestParam(defaultValue = "VNPAY") String paymentMethod,
                                 HttpSession session,
                                 HttpServletRequest request,
                                 Model model,
                                 RedirectAttributes ra) {

        // Validate thủ công
        if (seatIds == null || seatIds.isEmpty()) {
            model.addAttribute("errorMsg", "Vui lòng chọn ít nhất 1 ghế.");
            reloadSeatModel(showtimeId, model);
            return "customer/seat-select";
        }

        User user = userRepository.findById(currentUser(session).getUserId())
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        BookingDTO dto = new BookingDTO();
        dto.setShowtimeId(showtimeId);
        dto.setSeatIds(seatIds);
        dto.setPaymentMethod(paymentMethod);

        try {
            // Tạo đơn PENDING — ghế bị giữ tạm, chờ VNPay xác nhận
            Booking booking = bookingService.createPendingBooking(dto, user);

            // Lấy IP thực của client
            String ipAddress = getClientIp(request);

            // Tạo URL thanh toán VNPay
            String paymentUrl = vnPayService.createPaymentUrl(
                    booking.getBookingId(),
                    booking.getTotalAmount(),
                    ipAddress
            );

            // Redirect sang VNPay
            return "redirect:" + paymentUrl;

        } catch (RuntimeException e) {
            model.addAttribute("errorMsg", e.getMessage());
            reloadSeatModel(showtimeId, model);
            return "customer/seat-select";
        }
    }

    //Lịch sử đặt vé

    @GetMapping("/customer/bookings")
    public String bookingHistory(HttpSession session, Model model) {
        model.addAttribute("bookings",
                bookingService.getBookingHistory(currentUser(session).getUserId()));
        return "customer/booking-history";
    }

    @GetMapping("/customer/bookings/{id}")
    public String bookingDetail(@PathVariable Integer id,
                                HttpSession session, Model model) {
        model.addAttribute("booking",
                bookingService.getBookingDetail(id, currentUser(session).getUserId()));
        return "customer/booking-detail";
    }

    //Hủy vé

    @PostMapping("/customer/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable Integer id,
                                HttpSession session,
                                RedirectAttributes ra) {
        try {
            bookingService.cancelBooking(id, currentUser(session).getUserId());
            ra.addFlashAttribute("successMsg", "Đã hủy vé thành công. Ghế đã được giải phóng.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/customer/bookings/" + id;
    }

    // Helpers

    private SessionUser currentUser(HttpSession session) {
        return (SessionUser) session.getAttribute(AuthService.SESSION_KEY);
    }

    private void reloadSeatModel(Integer showtimeId, Model model) {
        Showtime st = showtimeService.getShowtimeById(showtimeId);
        model.addAttribute("showtime", st);
        model.addAttribute("seatMap",  showtimeService.getSeatMap(showtimeId));
        model.addAttribute("soldOut",  st.getStatus() == Showtime.Status.SOLD_OUT);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
        // Lấy IP đầu tiên nếu qua proxy
        if (ip.contains(",")) ip = ip.split(",")[0].trim();
        return ip;
    }
}