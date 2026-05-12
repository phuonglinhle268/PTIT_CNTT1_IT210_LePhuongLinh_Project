package org.example.java_web_project.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Bắt các exception không được xử lý ở Controller.
 *
 * Quan trọng: RuntimeException từ ownership check trong Service
 * ("Không tìm thấy đơn đặt vé") sẽ được bắt ở đây nếu Controller
 * không catch — hiển thị trang lỗi thay vì whitepage.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException ex,
                                         HttpServletRequest request,
                                         Model model) {
        log.warn("[GlobalException] {} — URI: {}", ex.getMessage(), request.getRequestURI());

        // Các message liên quan đến quyền truy cập → trang 403
        String msg = ex.getMessage();
        if (msg != null && (msg.contains("không có quyền")
                || msg.contains("Không tìm thấy đơn đặt vé")
                || msg.contains("Bạn không có quyền"))) {
            return "redirect:/403";
        }

        // Lỗi khác → trang error chung
        model.addAttribute("errorMsg", msg);
        return "error";
    }
}