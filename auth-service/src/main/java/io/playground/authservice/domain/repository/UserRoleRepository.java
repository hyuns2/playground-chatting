package io.playground.authservice.domain.repository;

import io.playground.authservice.domain.entity.Role;
import io.playground.authservice.domain.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    @Query("""
        select r from UserRole ur
        join ur.role r
        where ur.user.id = :userId
    """)
    List<Role> findRolesByUserId(String userId);
}
