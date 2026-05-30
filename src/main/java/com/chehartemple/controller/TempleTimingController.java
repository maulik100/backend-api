package com.chehartemple.controller;

import com.chehartemple.model.TempleTiming;
import com.chehartemple.repository.TempleTimingRepository;
import com.chehartemple.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TempleTimingController {

    private final TempleTimingRepository repository;
    private final AuditService auditService;

    @GetMapping("/temple-timings")
    public List<TempleTiming> getAll() {
        return repository.findAll();
    }

    @PostMapping("/admin/temple-timings")
    @PreAuthorize("hasRole('ADMIN')")
    public TempleTiming create(@RequestBody TempleTiming timing, HttpServletRequest request) {
        TempleTiming saved = repository.save(timing);
        auditService.log("CREATE_TIMING", "TIMING", String.valueOf(saved.getId()), "Created timing for: " + saved.getDay(), "ADMIN", request);
        return saved;
    }

    @PutMapping("/admin/temple-timings/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TempleTiming update(@PathVariable Long id, @RequestBody TempleTiming timing, HttpServletRequest request) {
        timing.setId(id);
        TempleTiming saved = repository.save(timing);
        auditService.log("UPDATE_TIMING", "TIMING", String.valueOf(id), "Updated timing for: " + saved.getDay(), "ADMIN", request);
        return saved;
    }

    @DeleteMapping("/admin/temple-timings/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id, HttpServletRequest request) {
        auditService.log("DELETE_TIMING", "TIMING", String.valueOf(id), "Deleted timing ID: " + id, "ADMIN", request);
        repository.deleteById(id);
    }
}
