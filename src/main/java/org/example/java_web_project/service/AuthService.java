package org.example.java_web_project.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.java_web_project.dto.LoginDTO;
import org.example.java_web_project.dto.RegisterDTO;
import org.example.java_web_project.dto.SessionUser;
import org.example.java_web_project.model.User;
import org.example.java_web_project.model.UserProfile;
import org.example.java_web_project.repository.UserProfileRepository;
import org.example.java_web_project.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository        userRepository;
    private final UserProfileRepository userProfileRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public static final String SESSION_KEY = "LOGGED_IN_USER";

    @Transactional
    public void register(RegisterDTO req) {
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp");
        }
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Username đã được sử dụng");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        // Mã hóa mật khẩu trước khi lưu
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(User.Role.CUSTOMER);
        userRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setFullname(req.getFullname());
        userProfileRepository.save(profile);
    }

    public SessionUser login(LoginDTO req, HttpSession session) {
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        if (Boolean.FALSE.equals(user.getStatus())) {
            throw new RuntimeException("Tài khoản đã bị khóa");
        }

        // So khớp mật khẩu qua BCrypt
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu không đúng");
        }

        UserProfile profile = userProfileRepository
                .findByUser_UserId(user.getUserId()).orElse(null);

        SessionUser sessionUser = new SessionUser(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                profile != null ? profile.getFullname() : "",
                profile != null ? profile.getAvatar() : null
        );

        session.setAttribute(SESSION_KEY, sessionUser);
        session.setMaxInactiveInterval(60 * 60);
        return sessionUser;
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }
}