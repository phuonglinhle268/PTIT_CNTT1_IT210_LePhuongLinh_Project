package org.example.java_web_project.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.java_web_project.service.BookingService;
import org.example.java_web_project.service.VNPayService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/payment")
public class PaymentController {

    private final VNPayService   vnPayService;
    private final BookingService bookingService;

    public PaymentController(VNPayService vnPayService, BookingService bookingService) {
        this.vnPayService   = vnPayService;
        this.bookingService = bookingService;
    }

    /**
     * VNPay redirect về URL này sau khi người dùng thanh toán.
     * URL được cấu hình tại vnpay.return-url trong application.properties.
     *
     * Luồng an toàn:
     *  1. Thu thập toàn bộ query params từ VNPay
     *  2. Xác minh chữ ký HMAC-SHA512 (verifyCallback)
     *  3. Kiểm tra mã phản hồi vnp_ResponseCode == "00" (thành công)
     *  4. Cập nhật DB tương ứng
     */
    @GetMapping("/vnpay-return")
    public String vnpayReturn(HttpServletRequest request,
                              RedirectAttributes ra) {

        // Thu thập tất cả params VNPay gửi về
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values.length > 0) params.put(key, values[0]);
        });

        String txnRef      = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");

        log.info("[VNPay Return] txnRef={}, responseCode={}", txnRef, responseCode);

        // Bước 1: Xác minh chữ ký — chặn giả mạo callback
        if (!vnPayService.verifyCallback(params)) {
            log.warn("[VNPay Return] Chữ ký không hợp lệ! txnRef={}", txnRef);
            ra.addFlashAttribute("errorMsg", "Phản hồi thanh toán không hợp lệ.");
            return "redirect:/movies";
        }

        // Bước 2: Lấy bookingId từ txnRef (format: bookingId_timestamp)
        Integer bookingId;
        try {
            bookingId = vnPayService.extractBookingId(txnRef);
        } catch (Exception e) {
            log.error("[VNPay Return] Không parse được txnRef={}", txnRef);
            ra.addFlashAttribute("errorMsg", "Lỗi xử lý kết quả thanh toán.");
            return "redirect:/movies";
        }

        // Bước 3: Xử lý kết quả
        if ("00".equals(responseCode)) {
            // Thanh toán thành công
            try {
                bookingService.confirmPayment(bookingId);
                ra.addFlashAttribute("successMsg", "Thanh toán thành công! Chúc bạn xem phim vui vẻ 🎬");
                return "redirect:/customer/bookings/" + bookingId;
            } catch (Exception e) {
                log.error("[VNPay Return] Lỗi confirm bookingId={}", bookingId, e);
                ra.addFlashAttribute("errorMsg", "Thanh toán thành công nhưng lỗi cập nhật đơn. Vui lòng liên hệ hỗ trợ.");
                return "redirect:/customer/bookings";
            }
        } else {
            // Thanh toán thất bại hoặc bị huỷ
            try {
                bookingService.failPayment(bookingId);
            } catch (Exception e) {
                log.error("[VNPay Return] Lỗi fail bookingId={}", bookingId, e);
            }
            String reason = getFailReason(responseCode);
            ra.addFlashAttribute("errorMsg", "Thanh toán không thành công: " + reason);
            return "redirect:/customer/bookings/" + bookingId;
        }
    }

    // ── Mã lỗi VNPay phổ biến ────────────────────────────────────────────────

    private String getFailReason(String code) {
        return switch (code) {
            case "07" -> "Trừ tiền thành công nhưng giao dịch bị nghi ngờ gian lận.";
            case "09" -> "Thẻ/tài khoản chưa đăng ký dịch vụ Internet Banking.";
            case "10" -> "Xác thực thông tin thẻ/tài khoản quá 3 lần.";
            case "11" -> "Đã hết hạn chờ thanh toán.";
            case "12" -> "Thẻ/tài khoản bị khóa.";
            case "13" -> "Sai OTP. Vui lòng thực hiện lại.";
            case "24" -> "Giao dịch bị hủy.";
            case "51" -> "Tài khoản không đủ số dư.";
            case "65" -> "Vượt hạn mức giao dịch trong ngày.";
            case "75" -> "Ngân hàng đang bảo trì.";
            default   -> "Lỗi không xác định (mã " + code + ").";
        };
    }
}