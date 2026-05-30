package com.chehartemple.controller;

import com.chehartemple.model.Event;
import com.chehartemple.repository.EventRepository;
import com.chehartemple.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EventController {

    private final EventRepository eventRepository;
    private final AuditService auditService;

    // Public: all active today + future events
    @GetMapping("/events")
    public List<Event> getActiveEvents() {
        return eventRepository.findByActiveTrueAndEventDateGreaterThanEqualOrderByEventDateAsc(LocalDate.now());
    }

    // Public: top 10 upcoming events for home screen
    @GetMapping("/events/home")
    public List<Event> getHomeEvents() {
        return eventRepository.findByActiveTrueAndEventDateGreaterThanEqualOrderByEventDateAsc(
                LocalDate.now(), PageRequest.of(0, 10));
    }

    // Public: today's events + next 7 upcoming (for events screen)
    @GetMapping("/events/limited")
    public Map<String, List<Event>> getLimitedEvents() {
        LocalDate today = LocalDate.now();
        Map<String, List<Event>> result = new LinkedHashMap<>();
        result.put("today", eventRepository.findByEventDateAndActiveTrue(today));
        result.put("upcoming", eventRepository.findByActiveTrueAndEventDateGreaterThanOrderByEventDateAsc(
                today, PageRequest.of(0, 7)));
        return result;
    }

    // Public: full categorized (today, upcoming, past)
    @GetMapping("/events/categorized")
    public Map<String, List<Event>> getCategorizedEvents() {
        LocalDate today = LocalDate.now();
        Map<String, List<Event>> result = new LinkedHashMap<>();
        result.put("today", eventRepository.findByEventDateAndActiveTrue(today));
        result.put("upcoming", eventRepository.findByActiveTrueAndEventDateGreaterThanOrderByEventDateAsc(today));
        result.put("past", eventRepository.findByEventDateBeforeOrderByEventDateDesc(today));
        return result;
    }

    // Admin: all categorized
    @GetMapping("/admin/events")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, List<Event>> getAllEventsCategorized() {
        LocalDate today = LocalDate.now();
        Map<String, List<Event>> result = new LinkedHashMap<>();
        result.put("today", eventRepository.findByEventDateAndActiveTrue(today));
        result.put("upcoming", eventRepository.findByActiveTrueAndEventDateGreaterThanOrderByEventDateAsc(today));
        result.put("past", eventRepository.findByEventDateBeforeOrderByEventDateDesc(today));
        return result;
    }

    @PostMapping("/admin/events")
    @PreAuthorize("hasRole('ADMIN')")
    public Event createEvent(@RequestBody Event event, HttpServletRequest request) {
        Event saved = eventRepository.save(event);
        auditService.log("CREATE_EVENT", "EVENT", String.valueOf(saved.getId()), "Created event: " + saved.getTitle(), "ADMIN", request);
        return saved;
    }

    @PutMapping("/admin/events/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Event updateEvent(@PathVariable Long id, @RequestBody Event event, HttpServletRequest request) {
        event.setId(id);
        Event saved = eventRepository.save(event);
        auditService.log("UPDATE_EVENT", "EVENT", String.valueOf(id), "Updated event: " + saved.getTitle(), "ADMIN", request);
        return saved;
    }

    @DeleteMapping("/admin/events/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id, HttpServletRequest request) {
        auditService.log("DELETE_EVENT", "EVENT", String.valueOf(id), "Deleted event ID: " + id, "ADMIN", request);
        eventRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
