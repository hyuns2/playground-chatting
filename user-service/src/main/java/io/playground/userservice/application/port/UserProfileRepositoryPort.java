package io.playground.userservice.application.port;

import io.playground.userservice.domain.UserProfile;

public interface UserProfileRepositoryPort {
    void save(UserProfile userProfile);
}
