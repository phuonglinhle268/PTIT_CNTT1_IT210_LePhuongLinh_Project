package org.example.java_web_project.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tickets")   // Bỏ uniqueConstraints — logic chặn ghế đã xử lý ở SeatRepository
@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Integer ticketId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Column(name = "ticket_price", precision = 10, scale = 2)
    private BigDecimal ticketPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_status")
    private TicketStatus ticketStatus;

    public enum TicketStatus {
        PENDING_PAYMENT,  // Đang giữ chỗ, chờ thanh toán VNPay
        BOOKED,           // Đã thanh toán thành công
        CANCELLED,        // Hủy (giải phóng ghế hoàn toàn)
        USED              // Đã sử dụng
    }
}