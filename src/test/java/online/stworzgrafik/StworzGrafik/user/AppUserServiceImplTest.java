package online.stworzgrafik.StworzGrafik.user;

import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchEntityService;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.RegionEntityService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;
import online.stworzgrafik.StworzGrafik.user.DTO.CreateUserRequest;
import online.stworzgrafik.StworzGrafik.user.DTO.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserServiceImplTest {
    @InjectMocks
    private AppUserServiceImpl appUserServiceImpl;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private StoreEntityService storeEntityService;

    @Mock
    private BranchEntityService branchEntityService;

    @Mock
    private RegionEntityService regionEntityService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private Store store;
    private Region region;
    private Branch branch;

    @BeforeEach
    void setup() {
        store = Store.builder().id(1L).name("TESTSTORE").build();
        region = Region.builder().id(1L).name("TESTREGION").build();
        branch = Branch.builder().id(1L).name("TESTBRANCH").build();
    }

    @Test
    void createUser_userWithThisLoginAlreadyExistsShouldThrowIllegalArgumentException() {
        //given
        CreateUserRequest createUserRequest = new CreateUserRequest("login", "password", UserRole.STORE_MANAGER, 1L, 1L, 1L, null);

        when(appUserRepository.existsByLogin(createUserRequest.login())).thenReturn(true);

        //when
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> appUserServiceImpl.create(createUserRequest));

        //then
        verify(appUserRepository, never()).save(any());
        assertEquals("Login already taken: login", exception.getMessage());
    }

    @Test
    void createUser_createAdminDoesNotThrowException() {
        //given
        CreateUserRequest createUserRequest = new CreateUserRequest("login", "password", UserRole.ADMIN, null, null, null, null);

        when(appUserRepository.existsByLogin(createUserRequest.login())).thenReturn(false);

        AppUser appUser = AppUser.builder()
                .id(1337L)
                .login(createUserRequest.login())
                .password("encodedPassword")
                .role(createUserRequest.role())
                .build();
        when(passwordEncoder.encode(createUserRequest.password())).thenReturn("encodedPassword");

        when(appUserRepository.save(any(AppUser.class))).thenReturn(appUser);

        //when
        UserResponse userResponse = appUserServiceImpl.create(createUserRequest);

        //then
        verify(appUserRepository, times(1)).save(any());
        verify(passwordEncoder).encode(createUserRequest.password());
        assertEquals(createUserRequest.login(), userResponse.login());
        assertEquals(createUserRequest.role(), userResponse.role());
    }

    @Test
    void createUser_createStoreManagerDoesNotThrowException() {
        //given
        CreateUserRequest createUserRequest = new CreateUserRequest("login", "password", UserRole.STORE_MANAGER, 1L, 1L, 1L, null);

        when(appUserRepository.existsByLogin(createUserRequest.login())).thenReturn(false);

        AppUser appUser = AppUser.builder()
                .id(1337L)
                .login(createUserRequest.login())
                .password("encodedPassword")
                .role(createUserRequest.role())
                .build();
        when(passwordEncoder.encode(createUserRequest.password())).thenReturn("encodedPassword");
        when(storeEntityService.getEntityById(1L)).thenReturn(store);
        when(appUserRepository.save(any(AppUser.class))).thenReturn(appUser);

        //when
        UserResponse userResponse = appUserServiceImpl.create(createUserRequest);

        //then
        verify(appUserRepository, times(1)).save(any());
        verify(passwordEncoder).encode(createUserRequest.password());

        assertEquals(createUserRequest.login(), userResponse.login());
        assertEquals(createUserRequest.role(), userResponse.role());
    }

    @Test
    void createUser_createStoreManagerWithoutStoreIdShouldThrowIllegalArgumentException() {
        //given
        CreateUserRequest createUserRequest = new CreateUserRequest("login", "password", UserRole.STORE_MANAGER, null, 1L, 1L, null);
        when(appUserRepository.existsByLogin(createUserRequest.login())).thenReturn(false);

        //when
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> appUserServiceImpl.create(createUserRequest));

        //then
        verify(appUserRepository, never()).save(any());
        assertEquals("StoreId is required for STORE_MANAGER", exception.getMessage());
    }

    @Test
    void createUser_createBranchDirectorDoesNotThrowException() {
        //given
        CreateUserRequest createUserRequest = new CreateUserRequest("login", "password", UserRole.DIRECTOR, null, 1L, 1L, DirectorScope.BRANCH);
        when(appUserRepository.existsByLogin(createUserRequest.login())).thenReturn(false);

        AppUser appUser = AppUser.builder()
                .login(createUserRequest.login())
                .password("encodedPassword")
                .role(createUserRequest.role())
                .branch(branch)
                .build();
        when(passwordEncoder.encode(createUserRequest.password())).thenReturn("encodedPassword");
        when(branchEntityService.getEntityById(createUserRequest.branchId())).thenReturn(branch);
        when(appUserRepository.save(any())).thenReturn(appUser);

        //when
        UserResponse userResponse = appUserServiceImpl.create(createUserRequest);

        //then
        verify(appUserRepository, times(1)).save(any());
        assertEquals(createUserRequest.login(), userResponse.login());
        assertEquals(createUserRequest.role(), userResponse.role());
    }

    @Test
    void createUser_createBranchDirectorWithoutBranchIdShouldThrowIllegalArgumentException() {
        //given
        CreateUserRequest createUserRequest = new CreateUserRequest("login", "password", UserRole.DIRECTOR, null, null, null, DirectorScope.BRANCH);
        when(appUserRepository.existsByLogin(createUserRequest.login())).thenReturn(false);

        //when
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> appUserServiceImpl.create(createUserRequest));

        //then
        verify(appUserRepository, never()).save(any());
        assertEquals("BranchId is required for BRANCH DIRECTOR", exception.getMessage());
    }

    @Test
    void createUser_createRegionDirectorDoesNotThrowException() {
        //given
        CreateUserRequest createUserRequest = new CreateUserRequest("login", "password", UserRole.DIRECTOR, null, null, 1L, DirectorScope.REGION);
        when(appUserRepository.existsByLogin(createUserRequest.login())).thenReturn(false);

        AppUser appUser = AppUser.builder()
                .login(createUserRequest.login())
                .password("encodedPassword")
                .role(createUserRequest.role())
                .build();
        when(passwordEncoder.encode(createUserRequest.password())).thenReturn("encodedPassword");
        when(regionEntityService.getEntityById(1L)).thenReturn(region);
        when(appUserRepository.save(any())).thenReturn(appUser);

        //when
        UserResponse userResponse = appUserServiceImpl.create(createUserRequest);

        //then
        verify(appUserRepository, times(1)).save(any());
        assertEquals(createUserRequest.login(), userResponse.login());
        assertEquals(createUserRequest.role(), userResponse.role());
    }

    @Test
    void createUser_createRegionDirectorWithoutRegionIdShouldThrowIllegalArgumentException() {
        //given
        CreateUserRequest createUserRequest = new CreateUserRequest("login", "password", UserRole.DIRECTOR, null, null, null, DirectorScope.REGION);
        when(appUserRepository.existsByLogin(createUserRequest.login())).thenReturn(false);

        //when
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> appUserServiceImpl.create(createUserRequest));

        //then
        verify(appUserRepository, never()).save(any());
        assertEquals("RegionId is required for REGION DIRECTOR", exception.getMessage());
    }

    @Test
    void createUser_createNetworkDirectorDoesNotThrowException(){
        //given
        CreateUserRequest createUserRequest = new CreateUserRequest("login", "password", UserRole.DIRECTOR, null, null, null, DirectorScope.NETWORK);
        when(appUserRepository.existsByLogin(createUserRequest.login())).thenReturn(false);

        AppUser appUser = AppUser.builder()
                .login(createUserRequest.login())
                .password("encodedPassword")
                .role(createUserRequest.role())
                .build();
        when(passwordEncoder.encode(createUserRequest.password())).thenReturn("encodedPassword");
        when(appUserRepository.save(any())).thenReturn(appUser);

        //when
        UserResponse userResponse = appUserServiceImpl.create(createUserRequest);

        //then
        verify(appUserRepository,times(1)).save(any());
        assertEquals(createUserRequest.login(), userResponse.login());
        assertEquals(createUserRequest.role(), userResponse.role());
    }

    @Test
    void changePassword_whenUserExists_shouldEncodeAndSave() {
        // given
        AppUser existingUser = AppUser.builder()
                .id(1L).login("user").password("oldEncoded").build();

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("newRawPass")).thenReturn("newEncoded");

        // when
        appUserServiceImpl.changePassword(1L, "newRawPass");

        // then
        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(captor.capture());

        assertEquals("newEncoded", captor.getValue().getPassword());
        verify(passwordEncoder).encode("newRawPass");
    }

    @Test
    void changePassword_whenUserNotFound_shouldThrowEntityNotFoundException() {
        when(appUserRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> appUserServiceImpl.changePassword(99L, "pass"));

        assertEquals("User not found: 99", ex.getMessage());
        verify(appUserRepository, never()).save(any());
    }

    @Test
    void setEnabled_whenUserExists_shouldSetEnabledTrueAndSave() {
        AppUser existingUser = AppUser.builder()
                .id(1L).login("user").enabled(false).build();

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        appUserServiceImpl.setEnabled(1L, true);

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(captor.capture());
        assertTrue(captor.getValue().isEnabled());
    }

    @Test
    void setEnabled_whenUserExists_shouldSetEnabledFalseAndSave() {
        AppUser existingUser = AppUser.builder()
                .id(1L).login("user").enabled(true).build();

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        appUserServiceImpl.setEnabled(1L, false);

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(captor.capture());
        assertFalse(captor.getValue().isEnabled());
    }

    @Test
    void setEnabled_whenUserNotFound_shouldThrowEntityNotFoundException() {
        when(appUserRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> appUserServiceImpl.setEnabled(99L, true));

        assertEquals("User not found: 99", ex.getMessage());
        verify(appUserRepository, never()).save(any());
    }

    @Test
    void findAll_whenUsersExist_shouldReturnMappedResponses() {
        AppUser user1 = AppUser.builder().id(1L).login("a").role(UserRole.ADMIN).build();
        AppUser user2 = AppUser.builder().id(2L).login("b").role(UserRole.STORE_MANAGER).build();

        when(appUserRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserResponse> result = appUserServiceImpl.findAll();

        assertEquals(2, result.size());
        assertEquals("a", result.get(0).login());
        assertEquals("b", result.get(1).login());
    }

    @Test
    void findAll_whenNoUsers_shouldReturnEmptyList() {
        when(appUserRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserResponse> result = appUserServiceImpl.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByLogin_whenUserExists_shouldReturnAppUser() {
        AppUser user = AppUser.builder().id(1L).login("jan").build();

        when(appUserRepository.findByLogin("jan")).thenReturn(Optional.of(user));

        AppUser result = appUserServiceImpl.findByLogin("jan");

        assertNotNull(result);
        assertEquals("jan", result.getLogin());
    }

    @Test
    void findByLogin_whenUserNotFound_shouldThrowEntityNotFoundException() {
        when(appUserRepository.findByLogin("ghost")).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> appUserServiceImpl.findByLogin("ghost"));

        assertEquals("Cannot find User by login ghost", ex.getMessage());
    }

    @Test
    void findStoreIdsByRegionId_shouldDelegateToRepository() {
        List<Long> expected = List.of(1L, 2L, 3L);
        when(appUserRepository.findStoreIdsByRegionId(5L)).thenReturn(expected);

        List<Long> result = appUserServiceImpl.findStoreIdsByRegionId(5L);

        assertEquals(expected, result);
        verify(appUserRepository).findStoreIdsByRegionId(5L);
    }

    @Test
    void findStoreIdsByBranchId_shouldDelegateToRepository() {
        List<Long> expected = List.of(10L, 20L);
        when(appUserRepository.findStoreIdsByBranchId(3L)).thenReturn(expected);

        List<Long> result = appUserServiceImpl.findStoreIdsByBranchId(3L);

        assertEquals(expected, result);
        verify(appUserRepository).findStoreIdsByBranchId(3L);
    }


    @Test
    void existsByLogin_whenLoginExists_shouldReturnTrue() {
        when(appUserRepository.existsByLogin("taken")).thenReturn(true);
        assertTrue(appUserServiceImpl.existsByLogin("taken"));
    }

    @Test
    void existsByLogin_whenLoginFree_shouldReturnFalse() {
        when(appUserRepository.existsByLogin("free")).thenReturn(false);
        assertFalse(appUserServiceImpl.existsByLogin("free"));
    }


    @Test
    void save_shouldDelegateToRepositoryAndReturnSavedEntity() {
        AppUser user = AppUser.builder().id(1L).login("user").build();
        when(appUserRepository.save(user)).thenReturn(user);

        AppUser result = appUserServiceImpl.save(user);

        assertSame(user, result);
        verify(appUserRepository).save(user);
    }
}