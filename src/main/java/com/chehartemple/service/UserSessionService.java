package com.chehartemple.service;

import com.chehartemple.dto.DeviceInfo;
import com.chehartemple.model.User;
import com.chehartemple.model.UserSession;
import com.chehartemple.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final UserSessionRepository sessionRepository;

    public String createSession(User user, DeviceInfo device, String ipAddress, String loginSource) {
        String sessionToken = UUID.randomUUID().toString();
        UserSession session = UserSession.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .deviceId(device != null ? device.getDeviceId() : null)
                .deviceName(device != null ? device.getDeviceName() : null)
                .deviceModel(device != null ? device.getDeviceModel() : null)
                .osName(device != null ? device.getOsName() : null)
                .osVersion(device != null ? device.getOsVersion() : null)
                .appVersion(device != null ? device.getAppVersion() : null)
                .ipAddress(ipAddress)
                .sessionToken(sessionToken)
                .loginSource(loginSource)
                .loginAt(LocalDateTime.now())
                .status(UserSession.SessionStatus.ACTIVE)
                .build();
        sessionRepository.save(session);
        return sessionToken;
    }

    public void endSession(String sessionToken) {
        sessionRepository.findBySessionTokenAndStatus(sessionToken, UserSession.SessionStatus.ACTIVE)
                .ifPresent(session -> {
                    session.setLogoutAt(LocalDateTime.now());
                    session.setStatus(UserSession.SessionStatus.LOGGED_OUT);
                    sessionRepository.save(session);
                });
    }

    public List<UserSession> getUserSessions(Long userId) {
        return sessionRepository.findByUserIdOrderByLoginAtDesc(userId);
    }

    public Page<UserSession> getAllSessions(int page, int size) {
        return sessionRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "loginAt")));
    }

    public List<UserSession> getActiveSessions() {
        return sessionRepository.findByStatus(UserSession.SessionStatus.ACTIVE);
    }
}
