package org.example.java_web_project.repository;


import org.example.java_web_project.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {
    List<Movie> findByStatus(Movie.Status status);
    List<Movie> findByTitleContainingIgnoreCase(String keyword);
}