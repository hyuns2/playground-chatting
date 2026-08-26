package io.playground.authservice.application;

import io.playground.authservice.domain.entity.Role;
import io.playground.authservice.domain.entity.User;
import io.playground.authservice.domain.entity.UserDevice;
import io.playground.authservice.domain.entity.UserRole;
import io.playground.authservice.domain.event.AuthSignUpEvent;
import io.playground.authservice.domain.repository.RoleRepository;
import io.playground.authservice.domain.repository.UserRepository;
import io.playground.authservice.domain.repository.UserRoleRepository;
import io.playground.authservice.exception.CustomErrorCode;
import io.playground.authservice.exception.CustomException;
import io.playground.authservice.infrastructure.redis.messaging.publisher.EventPublisher;
import io.playground.authservice.jwt.model.CustomPrincipal;
import io.playground.authservice.jwt.provider.JwtTokenProvider;
import io.playground.authservice.presentation.dto.AuthRequestDto;
import io.playground.authservice.presentation.dto.AuthResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final DeviceService deviceService;
    private final EventPublisher eventPublisher;

    @Transactional
    public void signUp(AuthRequestDto.SignUp dto) {
        if (userRepository.existsByEmail(dto.getEmail()))
            throw new CustomException(CustomErrorCode.USER_ALREADY_EXISTS);

        User user = userRepository.save(
                User.of(
                        dto.getEmail(),
                        passwordEncoder.encode(dto.getPassword())
                )
        );
        UserRole userRole = userRoleRepository.save(
                UserRole.of(
                        user,
                        roleRepository.getReferenceById(1L)
                )
        );
        user.addUserRoles(userRole);

        eventPublisher.handle(
                AuthSignUpEvent.of(
                        user.getId(),
                        dto.getName(),
                        dto.isPushAgree()
                )
        );
    }

    @Transactional
    public AuthResponseDto.SignIn signIn(AuthRequestDto.SignIn dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new CustomException(CustomErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword()))
            throw new CustomException(CustomErrorCode.USER_NOT_FOUND);

        UserDevice userDevice = deviceService.findOrCreateUserDevice(user.getId(), dto.getDeviceInfo());
        userDevice.updateLastSeenAt();

        return AuthResponseDto.SignIn.of(
                jwtTokenProvider.generateToken(
                        CustomPrincipal.of(user.getId(), userDevice.getDeviceId()),
                        user.getUserRoles().stream()
                                .map(userRole -> new SimpleGrantedAuthority(userRole.getRole().getRoleType().name()))
                                .toList()
                ),
                userDevice.getDeviceId()
        );
    }

    @Transactional
    public AuthResponseDto.Reissue reissueToken(AuthRequestDto.Reissue dto) {
        CustomPrincipal customPrincipal = jwtTokenProvider.validateRefreshToken(dto.getRefreshToken());
        List<Role> roles = userRoleRepository.findRolesByUserId(customPrincipal.getUserId());

        jwtTokenProvider.invalidateRefreshToken(customPrincipal);
        deviceService.updateLastSeenAtIfPresent(customPrincipal.getDeviceId());

        return AuthResponseDto.Reissue.of(
                jwtTokenProvider.generateToken(
                        customPrincipal,
                        roles.stream()
                                .map(role -> new SimpleGrantedAuthority(role.getRoleType().name()))
                                .toList()
                )
        );
    }
}
