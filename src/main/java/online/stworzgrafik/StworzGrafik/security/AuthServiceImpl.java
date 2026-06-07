package online.stworzgrafik.StworzGrafik.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.temporaryUser.AppUser;
import online.stworzgrafik.StworzGrafik.temporaryUser.AppUserDetailsService;
import online.stworzgrafik.StworzGrafik.temporaryUser.AppUserRepository;
import online.stworzgrafik.StworzGrafik.temporaryUser.DTO.AuthResponse;
import online.stworzgrafik.StworzGrafik.temporaryUser.DTO.LoginRequest;
import online.stworzgrafik.StworzGrafik.temporaryUser.UserRole;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
class AuthServiceImpl implements AuthService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AppUserDetailsService appUserDetailsService;

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        AppUser user = appUserRepository.findByLogin(loginRequest.login())
                .orElseThrow(() -> new BadCredentialsException("Invalid login or password"));

        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid login or password");
        }

        if (!user.isEnabled()) {
            throw new DisabledException("Account is disabled");
        }

        String token = jwtService.generateToken(user);

        String scopeName = resolveScopeName(user);

        return new AuthResponse(
                token,
                user.getLogin(),
                user.getRole().name(),
                user.getStore() != null ? user.getStore().getId() : null,
                user.getDirectorScope(),
                scopeName
        );
    }

    private String resolveScopeName(AppUser user) {
        // Kierownik sklepu — zwróć nazwę jego sklepu
        if (user.getRole() == UserRole.STORE_MANAGER) {
            return user.getStore() != null ? user.getStore().getName() : null;
        }

        // Dyrektor — zwróć nazwę oddziału / regionu / "Sieci"
        if (user.getDirectorScope() == null) return null;
        return switch (user.getDirectorScope()) {
            case BRANCH  -> user.getBranch() != null  ? user.getBranch().getName()  : null;
            case REGION  -> user.getRegion() != null  ? user.getRegion().getName()  : null;
            case NETWORK -> "Sieci";
        };
    }
}

