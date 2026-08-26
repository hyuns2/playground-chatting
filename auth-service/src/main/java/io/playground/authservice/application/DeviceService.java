package io.playground.authservice.application;

import io.playground.authservice.domain.entity.UserDevice;
import io.playground.authservice.domain.repository.UserDeviceRepository;
import io.playground.authservice.domain.repository.UserRepository;
import io.playground.authservice.presentation.dto.AuthRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final UserDeviceRepository userDeviceRepository;
    private final UserRepository userRepository;

    @Transactional
    public UserDevice findOrCreateUserDevice(String userId, AuthRequestDto.DeviceInfo deviceInfo) {
        List<UserDevice> userDevices = userDeviceRepository.findAllByUserId(userId);

        return findUserDeviceByDeviceId(userDevices, deviceInfo.getDeviceId())
                .orElseGet(() ->
                        saveNewUserDevice(userDevices,
                                UserDevice.of(
                                        userRepository.getReferenceById(userId),
                                        deviceInfo.getDeviceType(),
                                        deviceInfo.getDeviceName()
                                )
                        )
                );
    }

    private Optional<UserDevice> findUserDeviceByDeviceId(List<UserDevice> userDevices, String deviceId) {
        if (deviceId == null || deviceId.isBlank())
            return Optional.empty();

        for (UserDevice userDevice : userDevices)
            if (userDevice.isSameDevice(deviceId))
                return Optional.of(userDevice);

        return Optional.empty();
    }

    public UserDevice saveNewUserDevice(List<UserDevice> userDevices, UserDevice newUserDevice) {
        if (userDevices.size() > 2)
            userDevices.stream()
                    .min(Comparator.comparing(UserDevice::getLastSeenAt))
                    .ifPresent(userDeviceRepository::delete);

        return userDeviceRepository.save(newUserDevice);
    }

    @Transactional
    public void updateLastSeenAtIfPresent(String deviceId) {
        userDeviceRepository.findByDeviceId(deviceId)
                .ifPresent(UserDevice::updateLastSeenAt);
    }
}
