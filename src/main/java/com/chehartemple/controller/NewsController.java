package com.chehartemple.controller;

import com.chehartemple.model.News;
import com.chehartemple.repository.NewsRepository;
import com.chehartemple.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NewsController {

    private final NewsRepository newsRepository;
    private final AuditService auditService;

    @GetMapping("/news")
    public List<News> getLatestNews() {
        return newsRepository.findByActiveTrueOrderByCreatedAtDesc(PageRequest.of(0, 10));
    }

    @GetMapping("/admin/news")
    @PreAuthorize("hasRole('ADMIN')")
    public List<News> getAll() {
        return newsRepository.findAll();
    }

    @PostMapping("/admin/news")
    @PreAuthorize("hasRole('ADMIN')")
    public News create(@RequestBody News news, HttpServletRequest request) {
        News saved = newsRepository.save(news);
        auditService.log("CREATE_NEWS", "NEWS", String.valueOf(saved.getId()), "Created news: " + saved.getTitle(), "ADMIN", request);
        return saved;
    }

    @PutMapping("/admin/news/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public News update(@PathVariable Long id, @RequestBody News news, HttpServletRequest request) {
        news.setId(id);
        News saved = newsRepository.save(news);
        auditService.log("UPDATE_NEWS", "NEWS", String.valueOf(id), "Updated news: " + saved.getTitle(), "ADMIN", request);
        return saved;
    }

    @DeleteMapping("/admin/news/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        auditService.log("DELETE_NEWS", "NEWS", String.valueOf(id), "Deleted news ID: " + id, "ADMIN", request);
        newsRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
