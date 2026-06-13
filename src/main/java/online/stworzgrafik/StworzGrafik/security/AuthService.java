package online.stworzgrafik.StworzGrafik.security;

import online.stworzgrafik.StworzGrafik.user.DTO.AuthResponse;
import online.stworzgrafik.StworzGrafik.user.DTO.LoginRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
}
