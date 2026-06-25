package online.stworzgrafik.StworzGrafik.user;

import online.stworzgrafik.StworzGrafik.employee.DTO.ResponseEmployeeDTO;
import online.stworzgrafik.StworzGrafik.user.DTO.CreateUserRequest;
import online.stworzgrafik.StworzGrafik.user.DTO.SetRoleRequest;
import online.stworzgrafik.StworzGrafik.user.DTO.UserResponse;

import java.util.List;

public interface AppUserService {
    UserResponse create(CreateUserRequest createUserRequest);
    void changePassword(Long userId, String newPassword);
    void changeOwnPassword(String currentPassword, String newPassword);
    void setEnabled(Long userId, boolean enabled);
    UserResponse setRole(Long userId, SetRoleRequest request);
    List<UserResponse> findAll();
    AppUser findByLogin(String login);
    List<Long> findAllStoresIds();
    List<Long> findStoreIdsByRegionId(Long regionId);
    List<Long> findStoreIdsByBranchId(Long branchId);
    boolean existsByLogin(String login);
    AppUser save(AppUser appUser);
}
