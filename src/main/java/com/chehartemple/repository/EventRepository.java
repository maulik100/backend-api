package com.chehartemple.repository;

import com.chehartemple.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    // Public API: today + future, sorted nearest first
    List<Event> findByActiveTrueAndEventDateGreaterThanEqualOrderByEventDateAsc(LocalDate date);

    // Top N upcoming (for home screen)
    List<Event> findByActiveTrueAndEventDateGreaterThanEqualOrderByEventDateAsc(LocalDate date, Pageable pageable);

    // Today's events
    List<Event> findByEventDateAndActiveTrue(LocalDate date);

    // Upcoming (future only, excluding today)
    List<Event> findByActiveTrueAndEventDateGreaterThanOrderByEventDateAsc(LocalDate date);

    // Next N upcoming after today
    List<Event> findByActiveTrueAndEventDateGreaterThanOrderByEventDateAsc(LocalDate date, Pageable pageable);

    // Past events sorted most recent first
    List<Event> findByEventDateBeforeOrderByEventDateDesc(LocalDate date);
}
