package io.playground.authservice.infrastructure.redis.persistence;

import io.playground.authservice.jwt.config.JwtProperties;
import io.playground.authservice.jwt.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtProperties jwtProperties;

    private String getKeyName(String userId, String deviceId) {
        return KeyName.USER_PREFIX.getValue() + userId +
                KeyName.DEVICE_PREFIX + deviceId +
                KeyName.REFRESH_TOKEN;
    }

    public void saveOrUpdate(String userId, String deviceId, String token) {
        redisTemplate.opsForValue().set(
                getKeyName(userId, deviceId),
                token,
                Duration.ofMillis(jwtProperties.getRefreshExp())
        );
    }

    public Optional<String> findByUserIdAndDeviceKey(String userId, String deviceId) {
        String token = (String) redisTemplate.opsForValue().get(getKeyName(userId, deviceId));

        return Optional.ofNullable(token);
    }

    public void delete(String userId, String deviceId) {
        redisTemplate.delete(getKeyName(userId, deviceId));
    }
}
