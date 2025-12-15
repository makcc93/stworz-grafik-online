package online.stworzgrafik.StworzGrafik.temporaryUser;

import java.util.List;

public interface UserContext {
    Long getUserId();
    Long getUserStoreId();
    UserRole getUserRole();
    List<Long> getManagedStoreIds();
}
