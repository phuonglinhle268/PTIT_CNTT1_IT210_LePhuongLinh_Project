package org.example.java_web_project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.java_web_project.dto.ShowtimeDTO;
import org.example.java_web_project.dto.SeatStatusDTO;
import org.example.java_web_project.model.*;
import org.example.java_web_project.repository.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository    movieRepository;
    private final RoomRepository     roomRepository;
    private final SeatRepository     seatRepository;

    // ── Admin: Xem danh sách ─────────────────────────────────────────────────

    public List<Showtime> getAllShowtimes() {
        return showtimeRepository.findAllWithDetails();
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    // ── Admin: CORE-05 Tạo suất chiếu ───────────────────────────────────────

    @Transactional
    public void createShowtime(ShowtimeDTO dto) {
        Movie movie = movieRepository.findById(dto.getMovieId())
                .orElseThrow(() -> new RuntimeException("Phim không tồn tại"));
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        LocalDateTime startTime = dto.getStartTime();

        // ── Không cho chọn ngày/giờ trong quá khứ
        // (đã có @Future trong DTO nhưng validate lại ở service để an toàn)
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Thời gian bắt đầu không được ở trong quá khứ.");
        }

        // ── Không cho tạo suất chiếu trước ngày công chiếu của phim
        if (movie.getReleaseDate() != null
                && startTime.toLocalDate().isBefore(movie.getReleaseDate())) {
            throw new RuntimeException(String.format(
                    "Phim '%s' chưa công chiếu. Ngày công chiếu sớm nhất: %s.",
                    movie.getTitle(), movie.getReleaseDate()));
        }

        LocalDateTime endTime = startTime.plusMinutes(movie.getDuration() + 15);

        List<Showtime> conflicts = showtimeRepository.findConflicts(
                room.getRoomId(), startTime, endTime, -1);

        if (!conflicts.isEmpty()) {
            Showtime c = conflicts.get(0);
            throw new RuntimeException(String.format(
                    "Phòng '%s' đã có suất chiếu '%s' lúc %s. Phòng trống sau %s.",
                    room.getRoomName(),
                    c.getMovie().getTitle(),
                    c.getStartTime(),
                    c.getEndTime()
            ));
        }

        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(startTime);
        showtime.setEndTime(endTime);
        showtime.setWaitingTime(15);
        showtime.setBasePrice(dto.getBasePrice());
        showtime.setStatus(Showtime.Status.COMING);
        showtimeRepository.save(showtime);
    }

    // ── Admin: CORE-05 Cập nhật suất chiếu ──────────────────────────────────

    @Transactional
    public void updateShowtime(Integer id, ShowtimeDTO dto) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Suất chiếu không tồn tại"));

        // Không cho sửa nếu đã có vé CONFIRMED
        boolean hasConfirmedTickets = showtime.getTickets() != null
                && showtime.getTickets().stream()
                .anyMatch(t -> t.getTicketStatus() == Ticket.TicketStatus.BOOKED);
        if (hasConfirmedTickets) {
            throw new RuntimeException(
                    "Suất chiếu đã có khách đặt vé, không thể thay đổi giờ chiếu hoặc phòng.");
        }

        Movie movie = movieRepository.findById(dto.getMovieId())
                .orElseThrow(() -> new RuntimeException("Phim không tồn tại"));
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        LocalDateTime startTime = dto.getStartTime();

        // ── Không cho chọn ngày/giờ trong quá khứ
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Thời gian bắt đầu không được ở trong quá khứ.");
        }

        // ── Không cho tạo suất chiếu trước ngày công chiếu của phim
        if (movie.getReleaseDate() != null
                && startTime.toLocalDate().isBefore(movie.getReleaseDate())) {
            throw new RuntimeException(String.format(
                    "Phim '%s' chưa công chiếu. Ngày công chiếu sớm nhất: %s.",
                    movie.getTitle(), movie.getReleaseDate()));
        }

        LocalDateTime endTime = startTime.plusMinutes(movie.getDuration() + 15);

        // Kiểm tra xung đột — exclude chính suất đang sửa
        List<Showtime> conflicts = showtimeRepository.findConflicts(
                room.getRoomId(), startTime, endTime, id);

        if (!conflicts.isEmpty()) {
            Showtime c = conflicts.get(0);
            throw new RuntimeException(String.format(
                    "Phòng '%s' đã có suất chiếu '%s' lúc %s. Phòng trống sau %s.",
                    room.getRoomName(),
                    c.getMovie().getTitle(),
                    c.getStartTime(),
                    c.getEndTime()
            ));
        }

        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(startTime);
        showtime.setEndTime(endTime);
        showtime.setBasePrice(dto.getBasePrice());
        showtimeRepository.save(showtime);
    }

    // ── Admin: Hủy suất chiếu ────────────────────────────────────────────────

    @Transactional
    public void deleteShowtime(Integer id) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Suất chiếu không tồn tại"));
        showtime.setStatus(Showtime.Status.CANCELLED);
        showtimeRepository.save(showtime);
    }

    // ── CORE-08: Scheduler tự động cập nhật trạng thái ──────────────────────
    // Chạy mỗi 1 phút. Cần @EnableScheduling trong JavaWebProjectApplication.
    //
    // Logic:
    //   Movie  COMING_SOON → NOW_SHOWING : khi now >= releaseDate
    //   COMING             → SHOWING     : khi now >= startTime
    //   SHOWING            → ENDED       : khi now >= endTime

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void syncShowtimeStatuses() {
        LocalDateTime now = LocalDateTime.now();

        // ── Phim COMING_SOON → NOW_SHOWING khi đến ngày công chiếu
        List<Movie> toNowShowing = movieRepository
                .findByStatusAndReleaseDateLessThanEqual(
                        Movie.Status.COMING_SOON, now.toLocalDate());
        toNowShowing.forEach(m -> m.setStatus(Movie.Status.NOW_SHOWING));
        if (!toNowShowing.isEmpty()) {
            movieRepository.saveAll(toNowShowing);
            log.info("[Scheduler] Chuyển {} phim COMING_SOON → NOW_SHOWING", toNowShowing.size());
        }

        // ── Suất COMING → SHOWING khi đến giờ bắt đầu
        List<Showtime> toShowing = showtimeRepository
                .findByStatusAndStartTimeLessThanEqual(Showtime.Status.COMING, now);
        toShowing.forEach(s -> s.setStatus(Showtime.Status.SHOWING));
        if (!toShowing.isEmpty()) showtimeRepository.saveAll(toShowing);

        // ── Suất SHOWING → ENDED khi qua giờ kết thúc
        List<Showtime> toEnded = showtimeRepository
                .findByStatusAndEndTimeLessThanEqual(Showtime.Status.SHOWING, now);
        toEnded.forEach(s -> s.setStatus(Showtime.Status.ENDED));
        if (!toEnded.isEmpty()) showtimeRepository.saveAll(toEnded);

        // ── Suất SOLD_OUT đã qua giờ → ENDED
        List<Showtime> soldOutEnded = showtimeRepository
                .findByStatusAndEndTimeLessThanEqual(Showtime.Status.SOLD_OUT, now);
        soldOutEnded.forEach(s -> s.setStatus(Showtime.Status.ENDED));
        if (!soldOutEnded.isEmpty()) showtimeRepository.saveAll(soldOutEnded);

        log.debug("[Scheduler] Synced: movies={} +showing={} +ended={}",
                toNowShowing.size(), toShowing.size(),
                toEnded.size() + soldOutEnded.size());
    }

    // ── Customer: Danh sách suất chiếu của 1 phim ───────────────────────────
    // Chỉ hiển thị COMING và SOLD_OUT. ENDED / CANCELLED / SHOWING đều ẩn.

    public List<Showtime> getAvailableShowtimesByMovie(Integer movieId) {
        return showtimeRepository.findByMovie_MovieId(movieId)
                .stream()
                .filter(s -> s.getStatus() == Showtime.Status.COMING
                        || s.getStatus() == Showtime.Status.SOLD_OUT)
                .collect(Collectors.toList());
    }

    public Showtime getShowtimeById(Integer id) {
        return showtimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Suất chiếu không tồn tại"));
    }

    // ── Customer: Sơ đồ ghế ─────────────────────────────────────────────────

    public List<SeatStatusDTO> getSeatMap(Integer showtimeId) {
        Showtime showtime = getShowtimeById(showtimeId);
        Integer roomId = showtime.getRoom().getRoomId();

        List<Seat>    allSeats  = seatRepository
                .findByRoom_RoomIdOrderBySeatRowAscSeatNumberAsc(roomId);
        List<Integer> bookedIds = seatRepository
                .findBookedSeatIdsByShowtime(showtimeId);

        return allSeats.stream()
                .map(seat -> new SeatStatusDTO(
                        seat.getSeatId(),
                        seat.getSeatRow(),
                        seat.getSeatNumber(),
                        seat.getSeatType() != null ? seat.getSeatType().name() : "NORMAL",
                        bookedIds.contains(seat.getSeatId())
                ))
                .collect(Collectors.toList());
    }
}