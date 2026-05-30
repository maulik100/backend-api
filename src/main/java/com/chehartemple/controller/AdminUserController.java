package com.chehartemple.controller;

import com.chehartemple.model.User;
import com.chehartemple.repository.UserRepository;
import com.chehartemple.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;
    private final AuditService auditService;

    @GetMapping
    public Page<User> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search) {
        if (search.isEmpty()) {
            return userRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        }
        return userRepository.searchUsers(search, PageRequest.of(page, size));
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id, HttpServletRequest request) {
        auditService.log("DELETE_USER", "USER", String.valueOf(id), "Deleted user ID: " + id, "ADMIN", request);
        userRepository.deleteById(id);
    }
}
