package online.stworzgrafik.StworzGrafik.temporaryUser.roleStrategy;

import online.stworzgrafik.StworzGrafik.temporaryUser.UserContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component("DIRECTOR")
public class DirectorRoleStrategyImpl implements UserRoleStrategy{
        @Override
        public Long getAccessibleUserStoreId(UserContext user, Long storeId) {
            if (!user.getManagedStoreIds().contains(storeId)){
                   throw new AccessDeniedException("You do not have access to this store");
               }

               return storeId;
        }

    @Override
    public boolean hasAccessToStore(UserContext user, Long storeId) {
        return user.getManagedStoreIds().contains(storeId);
    }
}
