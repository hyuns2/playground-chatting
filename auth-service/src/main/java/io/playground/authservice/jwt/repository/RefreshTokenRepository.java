package io.playground.authservice.jwt.repository;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository {
    void saveOrUpdate(String userId, String deviceId, String token);

    Optional<String> findByUserIdAndDeviceKey(String userId, String deviceId);

    void delete(String userId, String deviceId);
}
