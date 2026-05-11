package org.example.java_web_project.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.java_web_project.model.Movie;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieDTO {

    @NotBlank(message = "Tên phim không được để trống")
    private String title;

    @NotBlank(message = "Mô tả không được để trống")
    private String description;

    @NotBlank(message = "Đạo diễn không được để trống")
    private String director;

    @NotBlank(message = "Diễn viên không được để trống")
    private String actors;

    @NotNull(message = "Thời lượng không được để trống")
    @Min(value = 1, message = "Thời lượng phải lớn hơn 0")
    private Integer duration;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;

    @NotBlank(message = "Poster không được để trống")
    private String poster;

    private String trailerUrl;

    private String ageRating;

    @NotNull(message = "Trạng thái không được để trống")
    private Movie.Status status;

    @NotEmpty(message = "Vui lòng chọn ít nhất một thể loại")
    private List<Integer> genreIds;
}