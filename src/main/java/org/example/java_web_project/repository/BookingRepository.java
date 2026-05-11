package org.example.java_web_project.repository;

import org.example.java_web_project.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    // ── CORE-07: Lịch sử ─────────────────────────────────────────────────────
    // Dùng 2 query tách biệt thay vì 1 query JOIN FETCH collection:
    //   Query 1: lấy danh sách booking (không fetch tickets)
    //   Hibernate tự batch-fetch tickets khi truy cập → không Cartesian product
    //   → DISTINCT không cần thiết, không bao giờ duplicate

    @Query("""
        SELECT b FROM Booking b
        WHERE b.user.userId = :userId
        ORDER BY b.bookingTime DESC
    """)
    List<Booking> findByUserId(@Param("userId") Integer userId);

    // Dùng cho getBookingDetail + cancelBooking — JOIN FETCH an toàn vì chỉ 1 booking
    @Query("""
        SELECT b FROM Booking b
        JOIN FETCH b.user
        JOIN FETCH b.tickets t
        JOIN FETCH t.showtime st
        JOIN FETCH st.movie
        JOIN FETCH st.room
        JOIN FETCH t.seat
        WHERE b.bookingId = :id
    """)
    Optional<Booking> findByIdWithDetails(@Param("id") Integer id);

    // Staff tra cứu mã
    @Query("""
        SELECT b FROM Booking b
        JOIN FETCH b.user
        JOIN FETCH b.tickets t
        JOIN FETCH t.showtime st
        JOIN FETCH st.movie
        JOIN FETCH st.room
        JOIN FETCH t.seat
        WHERE b.bookingCode = :code
    """)
    Optional<Booking> findByBookingCodeWithDetails(@Param("code") String code);

    //lấy các đơn PENDING quá thời hạn (10 phút)
    @Query("""
        SELECT b FROM Booking b
        JOIN FETCH b.tickets
        WHERE b.bookingStatus = org.example.java_web_project.model.Booking.BookingStatus.PENDING
          AND b.bookingTime < :expireTime
    """)
    List<Booking> findExpiredPendingBookings(@Param("expireTime") LocalDateTime expireTime);
}