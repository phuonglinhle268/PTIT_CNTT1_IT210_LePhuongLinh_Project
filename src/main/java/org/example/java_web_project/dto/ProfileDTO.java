package org.example.java_web_project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.java_web_project.model.UserProfile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDTO {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String fullname;

    @Pattern(regexp = "^(\\+84|0)[0-9]{9}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    private UserProfile.Gender gender;

    @Size(max = 200, message = "Địa chỉ tối đa 200 ký tự")
    private String address;
}
