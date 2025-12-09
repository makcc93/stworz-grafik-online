package online.stworzgrafik.StworzGrafik.temporaryUser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import online.stworzgrafik.StworzGrafik.store.Store;

import java.util.List;

@RequiredArgsConstructor
@Getter
@Setter
public class CurrentUser {
    private Long id;
    private Long storeId;
    private UserRole role;
    private List<Long> managedStoreIds;
}
