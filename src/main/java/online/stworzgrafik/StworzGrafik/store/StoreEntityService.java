package online.stworzgrafik.StworzGrafik.store;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public interface StoreEntityService {
    public Store saveEntity(@NotNull Store store);
    public Store getEntityById(@NotNull Long id);
}
