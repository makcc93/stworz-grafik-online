package online.stworzgrafik.StworzGrafik.store;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public interface StoreEntityService {
    Store saveEntity(@NotNull @Valid Store store);
    Store getEntityById(@NotNull Long id);
}
