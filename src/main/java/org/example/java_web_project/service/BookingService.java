package org.example.java_web_project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.java_web_project.dto.BookingDTO;
import org.example.java_web_project.model.*;
import org.example.java_web_project.repository.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository  bookingRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository     seatRepository;
    private final TicketRepository   ticketRepository;

    // ── Tạo đơn PENDING ──────────────────────────────────────────────────────

    @Transactional(rollbackFor = Exception.class)
    public Booking createPendingBooking(BookingDTO dto, User user) {
        Showtime showtime = showtimeRepository.findById(dto.getShowtimeId())
                .orElseThrow(() -> new RuntimeException("Suất chiếu không tồn tại"));

        if (showtime.getStatus() == Showtime.Status.ENDED
                || showtime.getStatus() == Showtime.Status.CANCELLED) {
            throw new RuntimeException("Suất chiếu này đã kết thúc hoặc bị hủy.");
        }
        if (showtime.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Suất chiếu này đã bắt đầu, không thể đặt vé.");
        }

        List<Integer> occupied = seatRepository.findBookedSeatIdsByShowtime(showtime.getShowtimeId());
        List<Integer> conflict = dto.getSeatIds().stream().filter(occupied::contains).toList();
        if (!conflict.isEmpty()) {
            throw new RuntimeException("Ghế " + conflict + " đang được giữ hoặc đã đặt. Vui lòng chọn lại.");
        }

        Booking booking = new Booking();
        booking.setBookingCode(generateCode());
        booking.setUser(user);
        booking.setBookingTime(LocalDateTime.now());
        booking.setPaymentMethod(Booking.PaymentMethod.VNPAY);
        booking.setPaymentStatus(Booking.PaymentStatus.PENDING);
        booking.setBookingStatus(Booking.BookingStatus.PENDING);

        List<Ticket> tickets = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Integer seatId : dto.getSeatIds()) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new RuntimeException("Ghế không tồn tại: " + seatId));
            BigDecimal price = showtime.getBasePrice()
                    .add(seat.getPriceExtra() != null ? seat.getPriceExtra() : BigDecimal.ZERO);

            Ticket ticket = new Ticket();
            ticket.setBooking(booking);
            ticket.setShowtime(showtime);
            ticket.setSeat(seat);
            ticket.setTicketPrice(price);
            ticket.setTicketStatus(Ticket.TicketStatus.PENDING_PAYMENT);
            tickets.add(ticket);
            total = total.add(price);
        }

        booking.setTotalAmount(total);
        booking.setTickets(tickets);
        bookingRepository.save(booking);
        return booking;
    }

    // ── VNPay: thành công ────────────────────────────────────────────────────

    @Transactional(rollbackFor = Exception.class)
    public void confirmPayment(Integer bookingId) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn: " + bookingId));

        if (booking.getBookingStatus() == Booking.BookingStatus.CONFIRMED) return;

        booking.setPaymentStatus(Booking.PaymentStatus.PAID);
        booking.setBookingStatus(Booking.BookingStatus.CONFIRMED);
        booking.getTickets().forEach(t -> t.setTicketStatus(Ticket.TicketStatus.BOOKED));
        bookingRepository.save(booking);

        updateShowtimeSoldOut(booking.getTickets().get(0).getShowtime());
    }

    // ── VNPay: thất bại / hủy ────────────────────────────────────────────────

    @Transactional(rollbackFor = Exception.class)
    public void failPayment(Integer bookingId) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn: " + bookingId));

        if (booking.getBookingStatus() == Booking.BookingStatus.CANCELLED) return;

        booking.setPaymentStatus(Booking.PaymentStatus.FAILED);
        booking.setBookingStatus(Booking.BookingStatus.CANCELLED);
        booking.getTickets().forEach(t -> t.setTicketStatus(Ticket.TicketStatus.CANCELLED));
        bookingRepository.save(booking);
    }

    // ── Scheduler: tự hủy đơn PENDING quá 10 phút ───────────────────────────
    // Chạy mỗi 1 phút. Cần @EnableScheduling ở main class.

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void expirePendingBookings() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(10);
        List<Booking> expired = bookingRepository.findExpiredPendingBookings(expireTime);

        if (expired.isEmpty()) return;

        for (Booking booking : expired) {
            booking.setPaymentStatus(Booking.PaymentStatus.FAILED);
            booking.setBookingStatus(Booking.BookingStatus.CANCELLED);
            booking.getTickets().forEach(t -> t.setTicketStatus(Ticket.TicketStatus.CANCELLED));
        }
        bookingRepository.saveAll(expired);
        log.info("[Scheduler] Đã hủy {} đơn PENDING quá 10 phút", expired.size());
    }

    private void updateShowtimeSoldOut(Showtime showtime) {
        int totalSeats  = showtime.getRoom().getTotalSeat();
        int bookedCount = seatRepository.findBookedSeatIdsByShowtime(showtime.getShowtimeId()).size();
        if (bookedCount >= totalSeats) {
            showtime.setStatus(Showtime.Status.SOLD_OUT);
            showtimeRepository.save(showtime);
        }
    }

    // ── CORE-07: Lịch sử ─────────────────────────────────────────────────────
    // Dùng findByUserId (không JOIN FETCH tickets) — Hibernate tự resolve
    // lazy load theo từng booking → không bao giờ duplicate

    public List<Booking> getBookingHistory(Integer userId) {
        return bookingRepository.findByUserId(userId);
    }

    public Booking getBookingDetail(Integer bookingId, Integer userId) {
        Booking b = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt vé"));
        if (!b.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xem đơn này");
        }
        return b;
    }

    // ── CORE-09: Hủy vé ─────────────────────────────────────────────────────

    @Transactional(rollbackFor = Exception.class)
    public void cancelBooking(Integer bookingId, Integer userId) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt vé"));

        if (!booking.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền hủy đơn này");
        }
        if (booking.getBookingStatus() == Booking.BookingStatus.CANCELLED) {
            throw new RuntimeException("Đơn này đã được hủy trước đó");
        }
        if (booking.getBookingStatus() == Booking.BookingStatus.PENDING) {
            throw new RuntimeException("Đơn đang chờ thanh toán, không thể hủy thủ công.");
        }

        LocalDateTime showStart = booking.getTickets().get(0).getShowtime().getStartTime();
        if (LocalDateTime.now().isAfter(showStart.minusHours(24))) {
            throw new RuntimeException(
                    "Chỉ có thể hủy trước 24 giờ so với giờ chiếu (chiếu lúc "
                            + showStart.toLocalTime() + " ngày " + showStart.toLocalDate() + ").");
        }

        booking.setBookingStatus(Booking.BookingStatus.CANCELLED);
        booking.setPaymentStatus(Booking.PaymentStatus.REFUNDED);
        booking.getTickets().forEach(t -> t.setTicketStatus(Ticket.TicketStatus.CANCELLED));
        bookingRepository.save(booking);

        Showtime showtime = booking.getTickets().get(0).getShowtime();
        if (showtime.getStatus() == Showtime.Status.SOLD_OUT) {
            showtime.setStatus(Showtime.Status.COMING);
            showtimeRepository.save(showtime);
        }
    }

    // ── Staff ─────────────────────────────────────────────────────────────────

    public Booking findByBookingCode(String code) {
        return bookingRepository.findByBookingCodeWithDetails(code)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn với mã: " + code));
    }

    private String generateCode() {
        return "BK" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 8).toUpperCase();
    }
}