package org.example.java_web_project.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor
public class ShowtimeDTO {

    @NotNull(message = "Vui lòng chọn phim")
    private Integer movieId;

    @NotNull(message = "Vui lòng chọn phòng chiếu")
    private Integer roomId;

    @NotNull(message = "Vui lòng chọn giờ bắt đầu")
    @Future(message = "Giờ bắt đầu phải là thời điểm trong tương lai")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startTime;

    @NotNull(message = "Vui lòng nhập giá vé")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá vé phải lớn hơn 0")
    private BigDecimal basePrice;

    // waiting_time dùng mặc định 15 phút, không cần nhập từ form
}