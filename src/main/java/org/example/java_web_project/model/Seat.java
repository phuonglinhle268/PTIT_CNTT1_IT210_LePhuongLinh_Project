package org.example.java_web_project.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "seats", uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "seat_row", "seat_number"}))
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer seatId;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "seat_row")
    private String seatRow;

    @Column(name = "seat_number")
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type")
    private SeatType seatType;

    @Column(name = "price_extra")
    private Double priceExtra;

    @OneToMany(mappedBy = "seat")
    private List<Ticket> tickets;
}
