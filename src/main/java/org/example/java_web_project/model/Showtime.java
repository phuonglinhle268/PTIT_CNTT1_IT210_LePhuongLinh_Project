package org.example.java_web_project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "showtimes")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Showtime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer showtimeId;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "waiting_time")
    private Integer waitingTime;

    @Column(name = "basePrice")
    private Double basePrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ShowtimeStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "showtime")
    private List<Ticket> tickets;
}
