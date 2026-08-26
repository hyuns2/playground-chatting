package io.playground.authservice.domain.repository;

import io.playground.authservice.domain.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    List<UserDevice> findAllByUserId(String userId);

    Optional<UserDevice> findByDeviceId(String deviceId);
}
