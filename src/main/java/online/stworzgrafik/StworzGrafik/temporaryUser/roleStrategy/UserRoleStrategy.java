package online.stworzgrafik.StworzGrafik.temporaryUser.roleStrategy;

import online.stworzgrafik.StworzGrafik.draft.DemandDraft;
import online.stworzgrafik.StworzGrafik.temporaryUser.UserContext;

import java.util.List;

public interface UserRoleStrategy {
    Long getAccessibleUserStoreId(UserContext user, Long storeId);
    boolean hasAccessToStore(UserContext user, Long storeId);
}
