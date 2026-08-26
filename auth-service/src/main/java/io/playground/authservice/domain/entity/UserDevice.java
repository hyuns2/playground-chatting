package io.playground.authservice.domain.entity;

import io.playground.authservice.domain.entity.type.DeviceType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    @Column(unique = true, nullable = false)
    private String deviceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceType deviceType;

    @Column(nullable = false)
    private String deviceName;

    @Column(nullable = false)
    private LocalDateTime lastSeenAt;

    public static UserDevice of(User user,
                                DeviceType deviceType,
                                String deviceName) {
        return UserDevice.builder()
                .user(user)
                .deviceId(UUID.randomUUID().toString())
                .deviceType(deviceType)
                .deviceName(deviceName)
                .lastSeenAt(LocalDateTime.now())
                .build();
    }

    public boolean isSameDevice(String deviceId) {
        return this.deviceId.equals(deviceId);
    }

    public void updateLastSeenAt() {
        this.lastSeenAt = LocalDateTime.now();
    }
}
