package io.playground.userservice.adapter.persistence;

import io.playground.userservice.application.port.UserProfileRepositoryPort;
import io.playground.userservice.domain.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserProfilePersistenceAdapter implements UserProfileRepositoryPort {
    private final UserProfileJpaRepository userProfileJpaRepository;

    @Override
    public void save(UserProfile userProfile) {
        userProfileJpaRepository.save(UserProfileEntity.from(userProfile));
    }
}
