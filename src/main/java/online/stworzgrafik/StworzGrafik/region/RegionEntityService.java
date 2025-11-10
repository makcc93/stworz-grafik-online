package online.stworzgrafik.StworzGrafik.region;

import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

@Validated
public interface RegionEntityService {
    public Region saveEntity(@Valid Region region);
    public Region getEntityById(@Valid Long id);
}
