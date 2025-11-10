package online.stworzgrafik.StworzGrafik.employee.position;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public interface PositionEntityService {
    public Position saveEntity(@NotNull Position position);
    public Position getEntityById(@NotNull Long id);
}
