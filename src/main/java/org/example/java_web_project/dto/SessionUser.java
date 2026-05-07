package org.example.java_web_project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Lưu vào HttpSession sau khi đăng nhập.
 * Không bao giờ chứa password.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SessionUser implements Serializable {
    private Integer userId;
    private String username;
    private String email;
    private String role;
    private String fullname;
    private String avatar;
}
