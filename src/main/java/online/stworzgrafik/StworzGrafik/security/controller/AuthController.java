package online.stworzgrafik.StworzGrafik.security.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.security.AuthService;
import online.stworzgrafik.StworzGrafik.temporaryUser.DTO.AuthResponse;
import online.stworzgrafik.StworzGrafik.temporaryUser.DTO.LoginRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest loginRequest){
        log.info("WCHODZE DO KONTROLLERA");
        log.info("login={}, password={}", loginRequest.login(),loginRequest.password());
        return ResponseEntity.ok(authService.login(loginRequest));
    }
}
