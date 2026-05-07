package org.example.java_web_project.service;


import lombok.RequiredArgsConstructor;
import org.example.java_web_project.dto.ProfileDTO;
import org.example.java_web_project.model.User;
import org.example.java_web_project.model.UserProfile;
import org.example.java_web_project.repository.UserProfileRepository;
import org.example.java_web_project.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    private static final String UPLOAD_DIR = "uploads/avatars/";

    // CORE-03: Lấy User (Controller sẽ đưa thẳng vào Model → Thymeleaf đọc)
    public User getUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
    }

    // CORE-03: Lấy UserProfile
    public UserProfile getProfile(Integer userId) {
        return userProfileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new RuntimeException("Hồ sơ không tồn tại"));
    }

    // CORE-03: Cập nhật hồ sơ
    @Transactional
    public void updateProfile(Integer userId, ProfileDTO req) {
        UserProfile profile = getProfile(userId);
        profile.setFullname(req.getFullname());
        profile.setPhone(req.getPhone());
        profile.setGender(req.getGender());
        profile.setAddress(req.getAddress());
        userProfileRepository.save(profile);
    }

    // CORE-03: Upload avatar
    @Transactional
    public String uploadAvatar(Integer userId, MultipartFile file) throws IOException {
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new RuntimeException("Chỉ chấp nhận file ảnh");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new RuntimeException("Ảnh không được vượt quá 2MB");
        }

        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf(".")) : ".jpg";
        String filename = "avatar_" + userId + "_" + UUID.randomUUID() + ext;

        Path dir = Paths.get(UPLOAD_DIR);
        Files.createDirectories(dir);
        Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);

        String avatarUrl = "/" + UPLOAD_DIR + filename;
        UserProfile profile = getProfile(userId);
        profile.setAvatar(avatarUrl);
        userProfileRepository.save(profile);

        return avatarUrl;
    }
}