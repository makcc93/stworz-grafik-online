package online.stworzgrafik.StworzGrafik.security;

import online.stworzgrafik.StworzGrafik.temporaryUser.DTO.AuthResponse;
import online.stworzgrafik.StworzGrafik.temporaryUser.DTO.LoginRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
}
