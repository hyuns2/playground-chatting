package io.playground.authservice.domain.repository;

import io.playground.authservice.domain.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {"userRoles"})
    Optional<User> findByEmail(String email);
}
