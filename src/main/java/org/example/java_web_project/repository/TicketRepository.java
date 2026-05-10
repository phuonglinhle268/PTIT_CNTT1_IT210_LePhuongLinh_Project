package org.example.java_web_project.repository;

import org.example.java_web_project.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Integer> { }
