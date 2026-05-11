package org.example.java_web_project.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "showtimes")
@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class Showtime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "showtime_id")
    private Integer showtimeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    // Thời gian dọn phòng (phút), mặc định 15'
    @Column(name = "waiting_time")
    private Integer waitingTime = 15;

    @Column(name = "base_price", precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "showtime", fetch = FetchType.LAZY)
    private List<Ticket> tickets;

    public enum Status { COMING, SHOWING, ENDED, SOLD_OUT, CANCELLED }

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }
}