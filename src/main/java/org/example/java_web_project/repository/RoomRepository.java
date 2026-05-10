package org.example.java_web_project.repository;

import org.example.java_web_project.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Integer> { }