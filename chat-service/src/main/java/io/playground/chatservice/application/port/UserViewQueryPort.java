package io.playground.chatservice.application.port;

import io.playground.chatservice.domain.UserView;

import java.util.List;

public interface UserViewQueryPort {
    List<UserView> findAllByUserIds(List<Long> userIds);
}
