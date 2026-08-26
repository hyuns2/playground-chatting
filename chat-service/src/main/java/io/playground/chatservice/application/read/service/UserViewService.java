package io.playground.chatservice.application.read.service;

import io.playground.chatservice.application.read.command.HandleUserProfileUpdatedCommand;
import io.playground.chatservice.application.read.model.UserView;
import io.playground.chatservice.application.read.port.UserViewQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserViewService implements UserViewUsecase {
    private final UserViewQueryPort userViewQueryPort;

    @Override
    public void handleUserProfileUpdated(HandleUserProfileUpdatedCommand command) {
        UserView model = userViewQueryPort.findById(command.userId())
                .orElse(
                        UserView.of(
                                command.userId(),
                                command.nickName(),
                                command.pushAgree(),
                                command.createdAt()
                        )
                );

        model.update(command.nickName(), command.pushAgree(), command.createdAt());

        userViewQueryPort.saveOrUpdate(model);
    }
}
