package online.stworzgrafik.StworzGrafik.security;

import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;
import online.stworzgrafik.StworzGrafik.user.AppUser;
import online.stworzgrafik.StworzGrafik.user.AppUserService;
import online.stworzgrafik.StworzGrafik.user.DTO.AuthResponse;
import online.stworzgrafik.StworzGrafik.user.DTO.LoginRequest;
import online.stworzgrafik.StworzGrafik.user.DirectorScope;
import online.stworzgrafik.StworzGrafik.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @InjectMocks
    private AuthServiceImpl authServiceImpl;

    @Mock
    private AppUserService appUserService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private final Long userId = 123L;
    private final String userLogin = "login";
    private final String userPassword = "password";
    private final String encodedPassword = "encodedPassword";
    private final String token = "token";
    private Branch branch;
    private Store store;
    private Region region;

    @BeforeEach
    void setup(){
        region = new TestRegionBuilder().build();
        branch = new TestBranchBuilder().withRegion(region).build();
        store = new TestStoreBuilder().withBranch(branch).build();
    }

    private AppUser.AppUserBuilder baseUser(){
        return AppUser.builder()
                .id(userId)
                .login(userLogin)
                .password(userPassword)
                .enabled(true);
    }

    @Test
    void login_workingTest(){
        //given
        AppUser appUser = baseUser()
                .role(UserRole.STORE_MANAGER)
                .branch(branch)
                .store(store)
                .directorScope(null)
                .build();

        when(appUserService.findByLogin(userLogin)).thenReturn(appUser);
        when(passwordEncoder.matches(any(),any())).thenReturn(true);
        when(jwtService.generateToken(appUser)).thenReturn(token);

        LoginRequest loginRequest = new LoginRequest(userLogin, userPassword);

        //when
        AuthResponse authResponse = authServiceImpl.login(loginRequest);

        //then
        assertEquals(userLogin,authResponse.login());
        assertEquals(UserRole.STORE_MANAGER.name(), authResponse.role());
        assertNull(authResponse.directorScope());
        assertEquals(token,authResponse.token());
    }

    @Test
    void login_invalidLoginOrPasswordThrowsException(){
        //given
        AppUser appUser = baseUser()
                .role(UserRole.STORE_MANAGER)
                .branch(branch)
                .store(store)
                .directorScope(null)
                .build();

        when(appUserService.findByLogin(userLogin)).thenReturn(appUser);
        when(passwordEncoder.matches(any(),any())).thenReturn(false);

        LoginRequest loginRequest = new LoginRequest(userLogin, userPassword);

        //when
        BadCredentialsException exception =
                assertThrows(BadCredentialsException.class, () -> authServiceImpl.login(loginRequest));

        //then
        assertEquals("Invalid login or password", exception.getMessage());
    }

    @Test
    void login_userNotFoundPropagatesException(){
        //given
        when(appUserService.findByLogin(userLogin))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Cannot find User by login " + userLogin));

        LoginRequest loginRequest = new LoginRequest(userLogin, userPassword);

        //when //then
        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> authServiceImpl.login(loginRequest));

        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_disabledAccountThrowsDisabledException(){
        //given
        AppUser appUser = baseUser()
                .role(UserRole.STORE_MANAGER)
                .branch(branch)
                .store(store)
                .directorScope(null)
                .enabled(false)
                .build();

        when(appUserService.findByLogin(userLogin)).thenReturn(appUser);
        when(passwordEncoder.matches(any(),any())).thenReturn(true);

        LoginRequest loginRequest = new LoginRequest(userLogin, userPassword);

        //when
        DisabledException exception =
                assertThrows(DisabledException.class, () -> authServiceImpl.login(loginRequest));

        //then
        assertEquals("Account is disabled", exception.getMessage());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_nullEnabledTreatedAsDisabled(){
        //given
        AppUser appUser = baseUser()
                .role(UserRole.STORE_MANAGER)
                .branch(branch)
                .store(store)
                .directorScope(null)
                .enabled(null)
                .build();

        when(appUserService.findByLogin(userLogin)).thenReturn(appUser);
        when(passwordEncoder.matches(any(),any())).thenReturn(true);

        LoginRequest loginRequest = new LoginRequest(userLogin, userPassword);

        //when //then
        assertThrows(DisabledException.class, () -> authServiceImpl.login(loginRequest));
    }

    @Test
    void login_storeManagerWithoutStoreReturnsNullScopeName(){
        //given
        AppUser appUser = baseUser()
                .role(UserRole.STORE_MANAGER)
                .branch(branch)
                .store(null)
                .directorScope(null)
                .build();

        when(appUserService.findByLogin(userLogin)).thenReturn(appUser);
        when(passwordEncoder.matches(any(),any())).thenReturn(true);
        when(jwtService.generateToken(appUser)).thenReturn(token);

        LoginRequest loginRequest = new LoginRequest(userLogin, userPassword);

        //when
        AuthResponse authResponse = authServiceImpl.login(loginRequest);

        //then
        assertNull(authResponse.scopeName());
        assertNull(authResponse.storeId());
    }

    @Test
    void login_storeManagerWithStoreReturnsStoreIdAndScopeName(){
        //given
        AppUser appUser = baseUser()
                .role(UserRole.STORE_MANAGER)
                .branch(branch)
                .store(store)
                .directorScope(null)
                .build();

        when(appUserService.findByLogin(userLogin)).thenReturn(appUser);
        when(passwordEncoder.matches(any(),any())).thenReturn(true);
        when(jwtService.generateToken(appUser)).thenReturn(token);

        LoginRequest loginRequest = new LoginRequest(userLogin, userPassword);

        //when
        AuthResponse authResponse = authServiceImpl.login(loginRequest);

        //then
        assertEquals(store.getId(), authResponse.storeId());
        assertEquals(store.getName(), authResponse.scopeName());
    }

    @Test
    void login_directorWithNullScopeReturnsNullScopeName(){
        //given
        AppUser appUser = baseUser()
                .role(UserRole.DIRECTOR)
                .branch(branch)
                .store(null)
                .directorScope(null)
                .build();

        when(appUserService.findByLogin(userLogin)).thenReturn(appUser);
        when(passwordEncoder.matches(any(),any())).thenReturn(true);
        when(jwtService.generateToken(appUser)).thenReturn(token);

        LoginRequest loginRequest = new LoginRequest(userLogin, userPassword);

        //when
        AuthResponse authResponse = authServiceImpl.login(loginRequest);

        //then
        assertNull(authResponse.scopeName());
        assertNull(authResponse.directorScope());
    }

    @Test
    void login_directorWithBranchScopeReturnsBranchName(){
        //given
        AppUser appUser = baseUser()
                .role(UserRole.DIRECTOR)
                .branch(branch)
                .store(null)
                .directorScope(DirectorScope.BRANCH)
                .build();

        when(appUserService.findByLogin(userLogin)).thenReturn(appUser);
        when(passwordEncoder.matches(any(),any())).thenReturn(true);
        when(jwtService.generateToken(appUser)).thenReturn(token);

        LoginRequest loginRequest = new LoginRequest(userLogin, userPassword);

        //when
        AuthResponse authResponse = authServiceImpl.login(loginRequest);

        //then
        assertEquals(DirectorScope.BRANCH, authResponse.directorScope());
        assertEquals(branch.getName(), authResponse.scopeName());
    }

    @Test
    void login_directorWithBranchScopeButNoBranchReturnsNullScopeName(){
        //given
        AppUser appUser = baseUser()
                .role(UserRole.DIRECTOR)
                .branch(null)
                .store(null)
                .directorScope(DirectorScope.BRANCH)
                .build();

        when(appUserService.findByLogin(userLogin)).thenReturn(appUser);
        when(passwordEncoder.matches(any(),any())).thenReturn(true);
        when(jwtService.generateToken(appUser)).thenReturn(token);

        LoginRequest loginRequest = new LoginRequest(userLogin, userPassword);

        //when
        AuthResponse authResponse = authServiceImpl.login(loginRequest);

        //then
        assertNull(authResponse.scopeName());
    }

    @Test
    void login_directorWithRegionScopeReturnsRegionName(){
        //given
        AppUser appUser = baseUser()
                .role(UserRole.DIRECTOR)
                .region(region)
                .store(null)
                .directorScope(DirectorScope.REGION)
                .build();

        when(appUserService.findByLogin(userLogin)).thenReturn(appUser);
        when(passwordEncoder.matches(any(),any())).thenReturn(true);
        when(jwtService.generateToken(appUser)).thenReturn(token);

        LoginRequest loginRequest = new LoginRequest(userLogin, userPassword);

        //when
        AuthResponse authResponse = authServiceImpl.login(loginRequest);

        //then
        assertEquals(DirectorScope.REGION, authResponse.directorScope());
        assertEquals(region.getName(), authResponse.scopeName());
    }

    @Test
    void login_directorWithRegionScopeButNoRegionReturnsNullScopeName(){
        //given
        AppUser appUser = baseUser()
                .role(UserRole.DIRECTOR)
                .region(null)
                .store(null)
                .directorScope(DirectorScope.REGION)
                .build();

        when(appUserService.findByLogin(userLogin)).thenReturn(appUser);
        when(passwordEncoder.matches(any(),any())).thenReturn(true);
        when(jwtService.generateToken(appUser)).thenReturn(token);

        LoginRequest loginRequest = new LoginRequest(userLogin, userPassword);

        //when
        AuthResponse authResponse = authServiceImpl.login(loginRequest);

        //then
        assertNull(authResponse.scopeName());
    }

    @Test
    void login_directorWithNetworkScopeReturnsSieci(){
        //given
        AppUser appUser = baseUser()
                .role(UserRole.DIRECTOR)
                .store(null)
                .directorScope(DirectorScope.NETWORK)
                .build();

        when(appUserService.findByLogin(userLogin)).thenReturn(appUser);
        when(passwordEncoder.matches(any(),any())).thenReturn(true);
        when(jwtService.generateToken(appUser)).thenReturn(token);

        LoginRequest loginRequest = new LoginRequest(userLogin, userPassword);

        //when
        AuthResponse authResponse = authServiceImpl.login(loginRequest);

        //then
        assertEquals("Sieci", authResponse.scopeName());
    }

    @Test
    void login_adminWithoutDirectorScopeReturnsNullScopeName(){
        //given
        AppUser appUser = baseUser()
                .role(UserRole.ADMIN)
                .store(null)
                .directorScope(null)
                .build();

        when(appUserService.findByLogin(userLogin)).thenReturn(appUser);
        when(passwordEncoder.matches(any(),any())).thenReturn(true);
        when(jwtService.generateToken(appUser)).thenReturn(token);

        LoginRequest loginRequest = new LoginRequest(userLogin, userPassword);

        //when
        AuthResponse authResponse = authServiceImpl.login(loginRequest);

        //then
        assertNull(authResponse.scopeName());
        assertEquals(UserRole.ADMIN.name(), authResponse.role());
    }

    @Test
    void login_tokenComesFromJwtServiceForGivenUser(){
        //given
        AppUser appUser = baseUser()
                .role(UserRole.STORE_MANAGER)
                .branch(branch)
                .store(store)
                .directorScope(null)
                .build();

        when(appUserService.findByLogin(userLogin)).thenReturn(appUser);
        when(passwordEncoder.matches(any(),any())).thenReturn(true);
        when(jwtService.generateToken(appUser)).thenReturn(token);

        LoginRequest loginRequest = new LoginRequest(userLogin, userPassword);

        //when
        AuthResponse authResponse = authServiceImpl.login(loginRequest);

        //then
        verify(jwtService).generateToken(appUser);
        assertEquals(token, authResponse.token());
    }

    @Test
    void login_passwordEncoderCalledWithRawAndEncodedPasswordInCorrectOrder(){
        //given
        AppUser appUser = baseUser()
                .password(encodedPassword)
                .role(UserRole.STORE_MANAGER)
                .branch(branch)
                .store(store)
                .directorScope(null)
                .build();

        when(appUserService.findByLogin(userLogin)).thenReturn(appUser);
        when(passwordEncoder.matches(userPassword, encodedPassword)).thenReturn(true);
        when(jwtService.generateToken(appUser)).thenReturn(token);

        LoginRequest loginRequest = new LoginRequest(userLogin, userPassword);

        //when
        authServiceImpl.login(loginRequest);

        //then
        verify(passwordEncoder).matches(userPassword, encodedPassword);
    }
}