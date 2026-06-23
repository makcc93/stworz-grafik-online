package online.stworzgrafik.StworzGrafik.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.user.AppUser;
import online.stworzgrafik.StworzGrafik.user.AppUserService;
import online.stworzgrafik.StworzGrafik.user.UserContext;
import online.stworzgrafik.StworzGrafik.user.UserRole;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class JwtAppUserContext implements UserContext {
    private final HttpServletRequest request;
    private final JwtService jwtService;
    private final AppUserService appUserService;

    public AppUser getUser(){
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")){
            throw new AccessDeniedException("No token");
        }

        String token = authorizationHeader.substring(7);
        String login = jwtService.extractUsername((token));
        return appUserService.findByLogin(login);
    }

    public Long getUserId() {
        return getUser().getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUserStoreId() {
        AppUser appUser = getUser();

        return appUser.getStore() != null ? appUser.getStore().getId() : null;
    }

    @Override
    public UserRole getUserRole() {
        return getUser().getRole();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getManagedStoreIds() {
        AppUser appUser = getUser();

        return switch (appUser.getRole()){
            case ADMIN -> List.of();
            case DIRECTOR -> getManagedStoreIdsForDirector(appUser);
            case STORE_MANAGER -> appUser.getStore() != null ? List.of(appUser.getStore().getId()) : List.of();
        };
    }

    private List<Long> getManagedStoreIdsForDirector(AppUser user) {
        if (user.getDirectorScope() == null) return List.of();
        return switch (user.getDirectorScope()) {
            case NETWORK -> List.of();
            case REGION -> appUserService.findStoreIdsByRegionId(user.getRegion().getId());
            case BRANCH -> appUserService.findStoreIdsByBranchId(user.getBranch().getId());
        };
    }
}