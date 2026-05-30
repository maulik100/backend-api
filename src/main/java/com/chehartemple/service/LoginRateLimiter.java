package com.chehartemple.service;

import com.chehartemple.exception.ApiException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS_PER_MINUTE = 10;
    private final Map<String, IpRecord> ipRecords = new ConcurrentHashMap<>();

    public void checkRateLimit(String ipAddress) {
        ipRecords.entrySet().removeIf(e -> e.getValue().isExpired());

        IpRecord record = ipRecords.computeIfAbsent(ipAddress, k -> new IpRecord());

        if (record.isExpired()) {
            record.reset();
        }

        if (record.count >= MAX_ATTEMPTS_PER_MINUTE) {
            throw ApiException.badRequest("Too many login attempts from your network. Please wait a minute and try again.");
        }

        record.count++;
    }

    private static class IpRecord {
        int count = 0;
        LocalDateTime windowStart = LocalDateTime.now();

        boolean isExpired() {
            return LocalDateTime.now().isAfter(windowStart.plusMinutes(1));
        }

        void reset() {
            count = 0;
            windowStart = LocalDateTime.now();
        }
    }
}
