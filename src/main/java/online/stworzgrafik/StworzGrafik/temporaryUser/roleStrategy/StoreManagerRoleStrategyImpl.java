package online.stworzgrafik.StworzGrafik.temporaryUser.roleStrategy;

import online.stworzgrafik.StworzGrafik.temporaryUser.UserContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component("STORE_MANAGER")
public class StoreManagerRoleStrategyImpl implements UserRoleStrategy {
    @Override
    public Long getAccessibleUserStoreId(UserContext user, Long storeId) {
        if (!user.getUserStoreId().equals(storeId)){
            throw new AccessDeniedException("You cannot take action with not your store");
        }

        return storeId;
    }

    @Override
    public boolean hasAccessToStore(UserContext user, Long storeId) {
        return user.getUserStoreId().equals(storeId);
    }
}
