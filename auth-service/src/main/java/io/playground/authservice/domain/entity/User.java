package io.playground.authservice.domain.entity;

import io.playground.authservice.domain.entity.type.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus userStatus;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserRole> userRoles;

    public static User of(String email, String password) {
        return User.builder()
                .email(email)
                .password(password)
                .userStatus(UserStatus.PENDING)
                .userRoles(new ArrayList<>())
                .build();
    }

    public void setUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }

    public void addUserRoles(UserRole userRole) {
        userRoles.add(userRole);
    }
}
