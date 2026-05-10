package org.example.java_web_project.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "rooms")
@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Integer roomId;

    @Column(name = "room_name", nullable = false, unique = true)
    private String roomName;

    @Column(name = "total_seat", nullable = false)
    private Integer totalSeat;

    // Lưu dưới dạng String thường (không dùng @Enumerated)
    // vì DB có giá trị "2D", "3D" không phải tên Java hợp lệ
    @Column(name = "room_type")
    private String roomType;

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private List<Seat> seats;
}