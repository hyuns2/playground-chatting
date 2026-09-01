package io.playground.chatservice.infrastructure.jpa.adapter;

import io.playground.chatservice.application.port.UserViewQueryPort;
import io.playground.chatservice.domain.UserView;
import io.playground.chatservice.infrastructure.jpa.entity.UserViewEntity;
import io.playground.chatservice.infrastructure.jpa.repository.UserViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserViewQueryAdapter implements UserViewQueryPort {
    private final UserViewRepository userViewRepository;

    @Override
    public List<UserView> findAllByUserIds(List<Long> userIds) {
        return userViewRepository.findAllByUserIdIn(userIds).stream()
                .map(UserViewEntity::toDomain)
                .toList();
    }
}
