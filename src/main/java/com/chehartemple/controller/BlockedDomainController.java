package com.chehartemple.controller;

import com.chehartemple.exception.ApiException;
import com.chehartemple.model.BlockedEmailDomain;
import com.chehartemple.repository.BlockedEmailDomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/blocked-domains")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BlockedDomainController {

    private final BlockedEmailDomainRepository repository;

    @GetMapping
    public ResponseEntity<List<BlockedEmailDomain>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> add(@RequestBody Map<String, String> body) {
        String domain = body.get("domain");
        String reason = body.get("reason");

        if (domain == null || domain.isBlank()) {
            throw ApiException.badRequest("Domain is required.");
        }

        domain = domain.toLowerCase().trim();

        if (repository.existsByDomain(domain)) {
            throw ApiException.conflict("Domain '" + domain + "' is already blocked.");
        }

        repository.save(BlockedEmailDomain.builder()
                .domain(domain)
                .reason(reason != null ? reason : "Disposable email service")
                .build());

        return ResponseEntity.ok(Map.of("message", "Domain '" + domain + "' blocked successfully."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> remove(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw ApiException.notFound("Blocked domain not found.");
        }
        repository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Domain unblocked successfully."));
    }
}
