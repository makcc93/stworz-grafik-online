package online.stworzgrafik.StworzGrafik.temporaryUser;

import online.stworzgrafik.StworzGrafik.temporaryUser.DTO.CreateUserRequest;
import online.stworzgrafik.StworzGrafik.temporaryUser.DTO.UserResponse;

import java.util.List;

public interface AppUserService {
    UserResponse create(CreateUserRequest createUserRequest);
    void changePassword(Long userId, String newPassword);
    void setEnabled(Long userId, boolean enabled);
    List<UserResponse> findAll();
    AppUser findByLogin(String login);
    List<Long> findStoreIdsByRegionId(Long regionId);
    List<Long> findStoreIdsByBranchId(Long branchId);
    boolean existsByLogin(String login);
    AppUser save(AppUser appUser);
}
