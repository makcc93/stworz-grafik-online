package online.stworzgrafik.StworzGrafik.user;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.branch.BranchEntityService;
import online.stworzgrafik.StworzGrafik.region.RegionEntityService;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import online.stworzgrafik.StworzGrafik.user.DTO.CreateUserRequest;
import online.stworzgrafik.StworzGrafik.user.DTO.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppUserServiceImpl implements AppUserService{
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final StoreEntityService storeEntityService;
    private final BranchEntityService branchEntityService;
    private final RegionEntityService regionEntityService;

    @Override
    public UserResponse create(CreateUserRequest createUserRequest) {
        if (appUserRepository.existsByLogin(createUserRequest.login())){
            throw new IllegalArgumentException("Login already taken: " + createUserRequest.login());
        }

        AppUser.AppUserBuilder appUserBuilder = AppUser.builder()
                .login(createUserRequest.login())
                .password(passwordEncoder.encode(createUserRequest.password()))
                .role(createUserRequest.role());

        switch(createUserRequest.role()){
            case STORE_MANAGER -> {
                if (createUserRequest.storeId() == null) throw new IllegalArgumentException("StoreId is required for STORE_MANAGER");
                appUserBuilder.store(storeEntityService.getEntityById(createUserRequest.storeId()));
            }
            case DIRECTOR -> {
                if (createUserRequest.directorScope() == null) throw new IllegalArgumentException("DirectorScope is required for DIRECTOR");
                appUserBuilder.directorScope(createUserRequest.directorScope());

                switch (createUserRequest.directorScope()){
                    case BRANCH -> {
                        if (createUserRequest.branchId() == null) throw new IllegalArgumentException("BranchId is required for BRANCH DIRECTOR");
                        appUserBuilder.branch(branchEntityService.getEntityById(createUserRequest.branchId()));
                    }
                    case REGION -> {
                        if (createUserRequest.regionId() == null) throw new IllegalArgumentException("RegionId is required for REGION DIRECTOR");
                        appUserBuilder.region(regionEntityService.getEntityById(createUserRequest.regionId()));
                    }
                    case NETWORK -> {}
                }
            }
            case ADMIN -> {}
        }

        AppUser savedAppUser = appUserRepository.save(appUserBuilder.build());
        log.info("Created user {} with role {}", savedAppUser.getLogin(), savedAppUser.getRole());

        return toResponse(savedAppUser);
    }

    @Override
    public void changePassword(Long userId, String newPassword) {
        AppUser appUser = appUserRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        appUser.setPassword(passwordEncoder.encode(newPassword));
        appUserRepository.save(appUser);
    }

    @Override
    public void setEnabled(Long userId, boolean enabled) {
        AppUser appUser = appUserRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        appUser.setEnabled(enabled);
        appUserRepository.save(appUser);
    }

    @Override
    public List<UserResponse> findAll() {
        return appUserRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AppUser findByLogin(String login) {
        return appUserRepository.findByLogin(login)
                .orElseThrow(() ->  new EntityNotFoundException("Cannot find User by login " + login));
    }

    @Override
    public List<Long> findStoreIdsByRegionId(Long regionId) {
        return appUserRepository.findStoreIdsByRegionId(regionId);
    }

    @Override
    public List<Long> findStoreIdsByBranchId(Long branchId) {
        return appUserRepository.findStoreIdsByBranchId(branchId);
    }

    @Override
    public boolean existsByLogin(String login) {
        return appUserRepository.existsByLogin(login);
    }

    @Override
    public AppUser save(AppUser appUser) {
        return appUserRepository.save(appUser);
    }

    private UserResponse toResponse(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getLogin(),
                user.getRole(),
                user.getDirectorScope(),
                user.getStore() != null ? user.getStore().getId() : null,
                user.getBranch() != null ? user.getBranch().getId() : null,
                user.getRegion() != null ? user.getRegion().getId() : null,
                user.getEnabled()
        );
    }
}
