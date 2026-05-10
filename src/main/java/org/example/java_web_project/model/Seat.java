package org.example.java_web_project.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "seats",
        uniqueConstraints = @UniqueConstraint(
                name = "unique_seat_in_room",
                columnNames = {"room_id", "seat_row", "seat_number"}
        ))
@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Integer seatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "seat_row", length = 1)
    private String seatRow;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type")
    private SeatType seatType;

    @Column(name = "price_extra", precision = 10, scale = 2)
    private BigDecimal priceExtra = BigDecimal.ZERO;

    public enum SeatType { NORMAL, VIP, COUPLE }
}