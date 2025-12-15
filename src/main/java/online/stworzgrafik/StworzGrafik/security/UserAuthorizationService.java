package online.stworzgrafik.StworzGrafik.security;

import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.temporaryUser.UserContext;
import online.stworzgrafik.StworzGrafik.temporaryUser.roleStrategy.UserRoleStrategy;
import online.stworzgrafik.StworzGrafik.temporaryUser.roleStrategy.UserRoleStrategyFactory;
import org.springframework.stereotype.Service;

@Service("userSecurityService")
@RequiredArgsConstructor
public class UserAuthorizationService {
    private final UserRoleStrategyFactory factory;
    private final UserContext user;

    public Long getUserAccessibleStoreId(Long storeId){
        UserRoleStrategy strategy = factory.getStrategy(user.getUserRole());
        return strategy.getAccessibleUserStoreId(user,storeId);
    }

    public boolean hasAccessToStore(Long storeId){
        UserRoleStrategy strategy = factory.getStrategy(user.getUserRole());
        return strategy.hasAccessToStore(user, storeId);
    }
}
