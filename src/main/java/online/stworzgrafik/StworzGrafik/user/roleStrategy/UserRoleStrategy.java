package online.stworzgrafik.StworzGrafik.user.roleStrategy;

import online.stworzgrafik.StworzGrafik.user.UserContext;

public interface UserRoleStrategy {
    Long getAccessibleUserStoreId(UserContext user, Long storeId);
    boolean hasAccessToStore(UserContext user, Long storeId);
}
