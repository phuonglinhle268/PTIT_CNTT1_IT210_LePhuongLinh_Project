package org.example.java_web_project.repository;

import org.example.java_web_project.model.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ShowtimeRepository extends JpaRepository<Showtime, Integer> {

    List<Showtime> findByMovie_MovieId(Integer movieId);

    /**
     * CORE-05: Kiểm tra xung đột phòng.
     * Tìm các suất chiếu trong cùng phòng mà khoảng thời gian bị chồng lên nhau.
     * Công thức: suất mới [newStart, newEnd] xung đột nếu newStart < existingEnd AND newEnd > existingStart
     *
     * @param roomId      phòng cần kiểm tra
     * @param newStart    giờ bắt đầu suất mới
     * @param newEnd      giờ kết thúc suất mới (đã tính cả waiting_time)
     * @param excludeId   id suất chiếu cần bỏ qua (dùng khi edit, truyền -1 nếu create)
     */
    @Query("""
        SELECT s FROM Showtime s
        WHERE s.room.roomId = :roomId
          AND s.status NOT IN (org.example.java_web_project.model.Showtime.Status.CANCELLED)
          AND s.showtimeId <> :excludeId
          AND :newStart < s.endTime
          AND :newEnd > s.startTime
    """)
    List<Showtime> findConflicts(
            @Param("roomId") Integer roomId,
            @Param("newStart") LocalDateTime newStart,
            @Param("newEnd") LocalDateTime newEnd,
            @Param("excludeId") Integer excludeId
    );

    // Tất cả suất chiếu còn hiệu lực (Admin quản lý)
    @Query("SELECT s FROM Showtime s JOIN FETCH s.movie JOIN FETCH s.room ORDER BY s.startTime DESC")
    List<Showtime> findAllWithDetails();

    List<Showtime> findByStatusAndStartTimeLessThanEqual(
            Showtime.Status status, LocalDateTime time);

    List<Showtime> findByStatusAndEndTimeLessThanEqual(
            Showtime.Status status, LocalDateTime time);
}