package org.example.java_web_project.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.java_web_project.dto.SessionUser;
import org.example.java_web_project.service.AuthService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * AuthInterceptor — Phân quyền chặt theo role.
 *
 * Mỗi role chỉ được vào đúng trang của mình:
 *   ADMIN    → /admin/**       (không vào /staff/**, /customer/**)
 *   STAFF    → /staff/**       (không vào /admin/**, /customer/**)
 *   CUSTOMER → /customer/**    (không vào /admin/**, /staff/**)
 *
 * Ownership (ai xem vé của ai) được xử lý tại tầng Service.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String uri = request.getRequestURI();
        String ctx = request.getContextPath();

        // Path công khai — không cần đăng nhập
        if (isPublicPath(uri, ctx)) {
            return true;
        }

        // Lấy session — không tạo mới nếu chưa có
        HttpSession session = request.getSession(false);
        SessionUser user = (session != null)
                ? (SessionUser) session.getAttribute(AuthService.SESSION_KEY)
                : null;

        // Chưa đăng nhập → lưu URL muốn vào rồi về trang login
        if (user == null) {
            String redirectAfterLogin = uri;
            if (request.getQueryString() != null) {
                redirectAfterLogin += "?" + request.getQueryString();
            }
            request.getSession(true).setAttribute("REDIRECT_AFTER_LOGIN", redirectAfterLogin);
            response.sendRedirect(ctx + "/auth/login");
            return false;
        }

        String role = user.getRole();

        // ── Phân quyền: mỗi role chỉ vào đúng trang của mình ────────────────

        boolean goingAdmin    = uri.startsWith(ctx + "/admin/")    || uri.startsWith("/admin/");
        boolean goingStaff    = uri.startsWith(ctx + "/staff/")    || uri.startsWith("/staff/");
        boolean goingCustomer = uri.startsWith(ctx + "/customer/") || uri.startsWith("/customer/");

        // ADMIN chỉ vào /admin/**
        if (goingAdmin && !"ADMIN".equals(role)) {
            response.sendRedirect(ctx + "/403");
            return false;
        }

        // STAFF chỉ vào /staff/**
        if (goingStaff && !"STAFF".equals(role)) {
            response.sendRedirect(ctx + "/403");
            return false;
        }

        // CUSTOMER chỉ vào /customer/**
        if (goingCustomer && !"CUSTOMER".equals(role)) {
            response.sendRedirect(ctx + "/403");
            return false;
        }

        // /profile/** → mọi role đã đăng nhập đều được
        return true;
    }

    private boolean isPublicPath(String uri, String ctx) {
        return uri.equals(ctx + "/")
                || uri.equals("/")
                || uri.startsWith(ctx + "/auth/")
                || uri.startsWith("/auth/")
                || uri.startsWith(ctx + "/css/")
                || uri.startsWith("/css/")
                || uri.startsWith(ctx + "/js/")
                || uri.startsWith("/js/")
                || uri.startsWith(ctx + "/images/")
                || uri.startsWith("/images/")
                || uri.startsWith(ctx + "/uploads/")
                || uri.startsWith("/uploads/")
                || uri.equals(ctx + "/403")
                || uri.equals("/403")
                || uri.startsWith(ctx + "/movies")
                || uri.startsWith("/movies")
                || uri.startsWith(ctx + "/payment/")
                || uri.startsWith("/payment/");
    }
}