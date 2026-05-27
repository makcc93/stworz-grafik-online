package online.stworzgrafik.StworzGrafik.temporaryUser.roleStrategy;

import online.stworzgrafik.StworzGrafik.temporaryUser.UserContext;
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
