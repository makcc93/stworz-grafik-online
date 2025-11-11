package online.stworzgrafik.StworzGrafik.region;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public interface RegionEntityService {
    public Region saveEntity(@NotNull Region region);
    public Region getEntityById(@NotNull Long id);
}
