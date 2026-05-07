package org.example.java_web_project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "rooms")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roomId;

    @Column(name = "room_name",unique = true)
    private String roomName;

    @Column(name = "total_seat")
    private Integer totalSeat;

    @Column(name = "room_type")
    private String roomType;

    @OneToMany(mappedBy = "room")
    private List<Seat> seats;

    @OneToMany(mappedBy = "room")
    private List<Showtime> showtimes;
}
