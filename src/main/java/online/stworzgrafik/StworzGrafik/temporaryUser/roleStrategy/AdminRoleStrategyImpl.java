package online.stworzgrafik.StworzGrafik.temporaryUser.roleStrategy;

import online.stworzgrafik.StworzGrafik.draft.DemandDraft;
import online.stworzgrafik.StworzGrafik.temporaryUser.UserContext;

import java.util.List;

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
