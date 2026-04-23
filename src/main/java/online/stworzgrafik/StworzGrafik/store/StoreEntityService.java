package online.stworzgrafik.StworzGrafik.store;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.UpdateStoreDTO;
import org.springframework.validation.annotation.Validated;

@Validated
public interface StoreEntityService {
    Store createEntityStore(@NotNull CreateStoreDTO dto);
    Store updateEntityStore(@NotNull Long storeId,@NotNull UpdateStoreDTO dto);
    Store saveEntity(@NotNull @Valid Store store);
    Store getEntityById(@NotNull Long id);
}
