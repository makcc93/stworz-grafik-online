package online.stworzgrafik.StworzGrafik.user.roleStrategy;

import online.stworzgrafik.StworzGrafik.user.UserContext;
import org.springframework.stereotype.Component;

@Component("ADMIN")
public class AdminRoleStrategyImpl implements UserRoleStrategy{
    @Override
    public Long getAccessibleUserStoreId(UserContext user, Long storeId) {
        return storeId;
    }

    @Override
    public boolean hasAccessToStore(UserContext user, Long storeId) {
        return true;
    }
}
