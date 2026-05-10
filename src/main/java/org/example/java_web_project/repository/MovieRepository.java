package org.example.java_web_project.repository;


import org.example.java_web_project.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {
    List<Movie> findByStatus(Movie.Status status);
    List<Movie> findByTitleContainingIgnoreCase(String keyword);
    List<Movie> findByStatusAndReleaseDateLessThanEqual(Movie.Status status, LocalDate date);
}