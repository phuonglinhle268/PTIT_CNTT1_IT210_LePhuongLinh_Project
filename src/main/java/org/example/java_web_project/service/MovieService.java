package org.example.java_web_project.service;

import lombok.RequiredArgsConstructor;
import org.example.java_web_project.dto.MovieDTO;
import org.example.java_web_project.model.Genre;
import org.example.java_web_project.model.Movie;
import org.example.java_web_project.repository.GenreRepository;
import org.example.java_web_project.repository.MovieRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;

    // CORE-04: Lấy danh sách tất cả phim (Admin xem)
    public List<Movie> getAllMovies() {
        //return movieRepository.findAll();
        return movieRepository.findAll(
                Sort.by(Sort.Direction.DESC, "movieId")
        );
    }

    // CORE-04: Lấy 1 phim theo id
    public Movie getMovieById(Integer id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phim không tồn tại"));
    }

    // CORE-04: Thêm phim mới
    @Transactional
    public void createMovie(MovieDTO req) {
        validateStatusVsReleaseDate(req.getStatus(), req.getReleaseDate());
        Movie movie = new Movie();
        mapRequestToMovie(req, movie);
        movieRepository.save(movie);
    }

    // CORE-04: Cập nhật phim
    @Transactional
    public void updateMovie(Integer id, MovieDTO req) {
        validateStatusVsReleaseDate(req.getStatus(), req.getReleaseDate());
        Movie movie = getMovieById(id);
        mapRequestToMovie(req, movie);
        movieRepository.save(movie);
    }

    // CORE-04: Xóa phim
    @Transactional
    public void deleteMovie(Integer id) {
        Movie movie = getMovieById(id);
        movieRepository.delete(movie);
    }

    // Lấy danh sách genre để hiển thị form (Admin chọn khi tạo/sửa phim)
    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }

    /**
     * Validate logic trạng thái vs ngày công chiếu:
     * - COMING_SOON yêu cầu releaseDate phải còn trong tương lai (> hôm nay)
     * - NOW_SHOWING yêu cầu releaseDate không được ở tương lai (phải <= hôm nay)
     */
    private void validateStatusVsReleaseDate(Movie.Status status, LocalDate releaseDate) {
        if (status == null) return;
        LocalDate today = LocalDate.now();

        if (status == Movie.Status.COMING_SOON) {
            if (releaseDate == null || !releaseDate.isAfter(today)) {
                throw new RuntimeException(
                        "Phim 'Sắp chiếu' phải có ngày công chiếu ở tương lai (sau ngày " + today + ").");
            }
        }

        if (status == Movie.Status.NOW_SHOWING) {
            if (releaseDate != null && releaseDate.isAfter(today)) {
                throw new RuntimeException(
                        "Phim 'Đang chiếu' không thể có ngày công chiếu ở tương lai. " +
                                "Vui lòng chọn trạng thái 'Sắp chiếu' hoặc điều chỉnh ngày công chiếu.");
            }
        }
    }

    // Map dữ liệu từ request vào entity (dùng chung cho create và update)
    private void mapRequestToMovie(MovieDTO req, Movie movie) {
        movie.setTitle(req.getTitle());
        movie.setDescription(req.getDescription());
        movie.setDirector(req.getDirector());
        movie.setActors(req.getActors());
        movie.setDuration(req.getDuration());
        movie.setReleaseDate(req.getReleaseDate());
        movie.setPoster(req.getPoster());
        movie.setTrailerUrl(req.getTrailerUrl());
        movie.setAgeRating(req.getAgeRating());
        movie.setStatus(req.getStatus());

        // Lấy danh sách Genre từ list id được chọn
        List<Genre> genres = genreRepository.findAllById(req.getGenreIds());
        movie.setGenres(genres);
    }
}