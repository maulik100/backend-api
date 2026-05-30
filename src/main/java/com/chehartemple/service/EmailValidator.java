package com.chehartemple.service;

import com.chehartemple.exception.ApiException;
import com.chehartemple.repository.BlockedEmailDomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmailValidator {

    private final BlockedEmailDomainRepository blockedEmailDomainRepository;

    private Set<String> cachedDomains = new HashSet<>();
    private LocalDateTime cacheExpiry = LocalDateTime.MIN;

    public void validate(String email) {
        if (email == null || email.isBlank()) {
            throw ApiException.badRequest("Email is required.");
        }

        String domain = email.substring(email.indexOf("@") + 1).toLowerCase().trim();

        if (getBlockedDomains().contains(domain)) {
            throw ApiException.badRequest("Temporary or disposable email addresses are not allowed. Please use a valid email (Gmail, Outlook, Yahoo, etc.).");
        }
    }

    private Set<String> getBlockedDomains() {
        if (LocalDateTime.now().isAfter(cacheExpiry)) {
            cachedDomains = new HashSet<>(blockedEmailDomainRepository.findAllDomains());
            cacheExpiry = LocalDateTime.now().plusMinutes(5);
        }
        return cachedDomains;
    }
}
