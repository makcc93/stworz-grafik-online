package online.stworzgrafik.StworzGrafik.temporaryUser;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
class CurrentUserThenMicroservice implements UserContext {
    private Long userId = 1L;
    private Long userStoreId = 1L;
    private UserRole userRole = UserRole.STORE_MANAGER;
    private List<Long> managedStoreIds = List.of(1L,2L);
    //pozniej tu wyzej beda dane pobierane z mikroservice user, teraz sa na sztywno wpisane, celowo
    //na przyklad tak:
//    private final UserMicroserviceClient userClient;
//    private UserDataFromMicroservice userData;
//
//    public CurrentUserFromMicroservice(UserMicroserviceClient userClient) {
//        this.userClient = userClient;
//        // W momencie tworzenia obiektu (w kontekście requestu)
//        // odpytujemy mikroserwis, aby uzyskać wszystkie dane
//        this.userData = userClient.getCurrentUserData();
//    }

    @Override
    public Long getUserId() {
        return userId;
    }

    @Override
    public Long getUserStoreId() {
        return userStoreId;
    }

    @Override
    public UserRole getUserRole() {
        return userRole;
    }

    @Override
    public List<Long> getManagedStoreIds() {
        return managedStoreIds;
    }
}
