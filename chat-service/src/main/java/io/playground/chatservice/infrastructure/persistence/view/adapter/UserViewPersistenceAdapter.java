package io.playground.chatservice.infrastructure.persistence.view.adapter;

import io.playground.chatservice.application.read.model.UserView;
import io.playground.chatservice.application.read.port.UserViewQueryPort;
import io.playground.chatservice.infrastructure.persistence.view.entity.UserViewJpaEntity;
import io.playground.chatservice.infrastructure.persistence.view.repository.UserViewJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserViewPersistenceAdapter implements UserViewQueryPort {
    private final UserViewJpaRepository userViewRepository;

    @Override
    public Optional<UserView> findById(String userId) {
        UserViewJpaEntity entity = userViewRepository.findById(userId)
                .orElse(null);

        return Optional.ofNullable(
                entity != null ?
                        entity.toDomain() :
                        null
        );
    }

    @Override
    public List<UserView> findAllByIds(List<String> userIds) {
        return userViewRepository.findAllById(userIds).stream()
                .map(UserViewJpaEntity::toDomain)
                .toList();
    }

    @Override
    public void saveOrUpdate(UserView userView) {
        UserViewJpaEntity entity = userViewRepository.findById(userView.getUserId())
                        .orElse(UserViewJpaEntity.ofEmpty());

        entity.update(
                userView.getUserId(),
                userView.getNickName(),
                userView.isPushAgree(),
                userView.getUpdatedAt()
        );

        userViewRepository.save(entity);
    }
}
