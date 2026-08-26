package io.playground.userservice.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileJpaRepository extends JpaRepository<UserProfileEntity, String> {
}
