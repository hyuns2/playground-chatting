package io.playground.chatservice.infrastructure.jpa.adapter;

import io.playground.chatservice.domain.UserView;
import io.playground.chatservice.infrastructure.jpa.entity.UserViewEntity;
import io.playground.chatservice.infrastructure.jpa.repository.UserViewRepository;
import io.playground.chatservice.testsupport.DataJpaTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = DataJpaTestConfig.class)
@TestPropertySource(properties = {
        "spring.profiles.active=test",
        "spring.sql.init.mode=never"
})
@Import(UserViewQueryAdapter.class)
@DisplayName("UserViewQueryAdapter 통합 테스트")
class UserViewQueryAdapterTest {

    @Autowired
    private UserViewQueryAdapter sut;

    @Autowired
    private UserViewRepository userViewRepository;

    @BeforeEach
    void setUp() {
        userViewRepository.save(new UserViewEntity(null, 1L, "user1", Instant.now()));
        userViewRepository.save(new UserViewEntity(null, 2L, "user2", Instant.now()));
        userViewRepository.save(new UserViewEntity(null, 3L, "user3", Instant.now()));
    }

    @Test
    @DisplayName("요청한 userId 목록에 해당하는 UserView만 반환한다")
    void findsMatchingUserViews() {
        List<UserView> result = sut.findAllByUserIds(List.of(1L, 3L, 999L));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserView::getUserId).containsExactlyInAnyOrder(1L, 3L);
    }

    @Test
    @DisplayName("일치하는 userId가 없으면 빈 목록을 반환한다")
    void returnsEmptyWhenNoneMatch() {
        List<UserView> result = sut.findAllByUserIds(List.of(999L));

        assertThat(result).isEmpty();
    }
}
