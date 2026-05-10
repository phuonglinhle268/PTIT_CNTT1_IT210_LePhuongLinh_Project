package org.example.java_web_project.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter @Setter @NoArgsConstructor
public class BookingDTO {
    // Không @NotNull/@NotEmpty ở đây — validate thủ công trong controller
    // vì Spring MVC không bind hidden inputs khi form submit đúng cách hơn
    private Integer showtimeId;
    private List<Integer> seatIds;
    private String paymentMethod = "VNPAY";
}