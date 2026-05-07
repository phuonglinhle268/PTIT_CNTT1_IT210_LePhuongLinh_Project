package org.example.java_web_project.repository;


import org.example.java_web_project.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {

    Optional<UserProfile> findByUser_UserId(Integer userId);
}
