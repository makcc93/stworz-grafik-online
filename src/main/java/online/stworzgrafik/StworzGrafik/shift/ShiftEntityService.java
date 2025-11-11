package online.stworzgrafik.StworzGrafik.shift;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public interface ShiftEntityService {
    public Shift saveEntity(@NotNull Shift shift);
    public Shift getEntityById(@NotNull Long id);
}
