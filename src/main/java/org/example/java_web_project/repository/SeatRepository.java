package org.example.java_web_project.repository;

import org.example.java_web_project.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Integer> {

    List<Seat> findByRoom_RoomIdOrderBySeatRowAscSeatNumberAsc(Integer roomId);

    /**
     * Ghế bị coi là "đã đặt" (không thể chọn) khi:
     *   - BOOKED          : đã thanh toán thành công
     *   - PENDING_PAYMENT : đang giữ chỗ chờ ai đó thanh toán VNPay
     *
     * CANCELLED hoàn toàn tự do → người khác đặt lại được bình thường.
     * Không dùng <> CANCELLED vì nếu thêm enum mới sẽ không an toàn.
     * Dùng IN (BOOKED, PENDING_PAYMENT) để explicit hơn.
     */
    @Query("""
        SELECT t.seat.seatId FROM Ticket t
        WHERE t.showtime.showtimeId = :showtimeId
          AND t.ticketStatus IN (
              org.example.java_web_project.model.Ticket.TicketStatus.BOOKED,
              org.example.java_web_project.model.Ticket.TicketStatus.PENDING_PAYMENT
          )
    """)
    List<Integer> findBookedSeatIdsByShowtime(@Param("showtimeId") Integer showtimeId);
}