package online.stworzgrafik.StworzGrafik.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.temporaryUser.AppUser;
import online.stworzgrafik.StworzGrafik.temporaryUser.AppUserDetailsService;
import online.stworzgrafik.StworzGrafik.temporaryUser.AppUserRepository;
import online.stworzgrafik.StworzGrafik.temporaryUser.DTO.AuthResponse;
import online.stworzgrafik.StworzGrafik.temporaryUser.DTO.LoginRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
class AuthServiceImpl implements AuthService{
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AppUserDetailsService appUserDetailsService;

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        AppUser user = appUserRepository.findByLogin(loginRequest.login())
                .orElseThrow(() -> new BadCredentialsException("Invalid login or password"));

        log.info("AuthServiceImpl, user login ={} ({})", loginRequest.login(), user.getLogin());
        log.info("AuthServiceImpl, user password ={} ({})", loginRequest.password(), user.getPassword());
        log.info("AuthServiceImpl, does it match? = {}", passwordEncoder.matches(loginRequest.password(), user.getPassword()));

        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())){
            throw new BadCredentialsException("Invalid login or password");
        }

        if (!user.isEnabled()){
            throw new DisabledException("Account is disabled");
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                user.getLogin(),
                user.getRole().name(),
                user.getStore() != null ? user.getStore().getId() : null
        );
    }
}
