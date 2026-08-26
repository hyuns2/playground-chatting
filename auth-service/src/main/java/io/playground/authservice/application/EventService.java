package io.playground.authservice.application;

import io.playground.authservice.domain.entity.User;
import io.playground.authservice.domain.entity.type.UserStatus;
import io.playground.authservice.domain.repository.UserRepository;
import io.playground.authservice.exception.CustomErrorCode;
import io.playground.authservice.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventService {
    private final UserRepository userRepository;

    @Transactional
    public void activateSignUp(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.USER_NOT_FOUND));

        user.setUserStatus(UserStatus.ACTIVE);
    }

    @Transactional
    public void rollbackSignUp(String userId) {
        userRepository.deleteById(userId);
    }
}
