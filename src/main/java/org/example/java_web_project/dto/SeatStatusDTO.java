package org.example.java_web_project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class SeatStatusDTO {
    private Integer seatId;
    private String seatRow;
    private Integer seatNumber;
    private String seatType;   // NORMAL / VIP / COUPLE
    private boolean booked;    // true = đã có người đặt
}