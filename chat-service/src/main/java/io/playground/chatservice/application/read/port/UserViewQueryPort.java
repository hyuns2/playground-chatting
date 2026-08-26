package io.playground.chatservice.application.read.port;

import io.playground.chatservice.application.read.model.UserView;

import java.util.List;
import java.util.Optional;

public interface UserViewQueryPort {
    Optional<UserView> findById(String userId);

    List<UserView> findAllByIds(List<String> userIds);

    void saveOrUpdate(UserView userView);
}
