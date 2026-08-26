package io.playground.userservice.application.service;

import io.playground.userservice.application.command.CreateUserProfileCommand;
import io.playground.userservice.application.port.UserProfileRepositoryPort;
import io.playground.userservice.domain.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements UserUsecase {
    private final UserProfileRepositoryPort userProfileRepositoryPort;

    @Transactional
    @Override
    public void createUserProfile(CreateUserProfileCommand command) {
        UserProfile userProfile = UserProfile.of(
                command.userId(),
                command.name(),
                command.pushAgree()
        );

        userProfileRepositoryPort.save(userProfile);
    }
}
