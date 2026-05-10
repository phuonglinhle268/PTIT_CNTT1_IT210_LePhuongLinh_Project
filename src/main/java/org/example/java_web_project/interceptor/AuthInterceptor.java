package org.example.java_web_project.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.java_web_project.dto.SessionUser;
import org.example.java_web_project.service.AuthService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();
        String ctx = request.getContextPath();

        if (isPublicPath(uri)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        SessionUser user = (session != null)
                ? (SessionUser) session.getAttribute(AuthService.SESSION_KEY)
                : null;

        if (user == null) {
            response.sendRedirect(ctx + "/auth/login");
            return false;
        }

        String role = user.getRole();

        // Phân quyền
        if (uri.startsWith(ctx + "/admin/") && !"ADMIN".equals(role)) {
            response.sendRedirect(ctx + "/403");
            return false;
        }
        if (uri.startsWith(ctx + "/staff/") && !"STAFF".equals(role) && !"ADMIN".equals(role)) {
            response.sendRedirect(ctx + "/403");
            return false;
        }
        if (uri.startsWith(ctx + "/customer/") && !"CUSTOMER".equals(role) && !"ADMIN".equals(role)) {
            response.sendRedirect(ctx + "/403");
            return false;
        }

        return true;
    }

    private boolean isPublicPath(String uri) {
        return uri.equals("/")
                || uri.startsWith("/auth/")
                || uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.startsWith("/images/")
                || uri.startsWith("/uploads/")
                || uri.startsWith("/403")
                || uri.startsWith("/movies")
                || uri.startsWith("/payment/") ;  // VNPay callback không cần login
    }
}