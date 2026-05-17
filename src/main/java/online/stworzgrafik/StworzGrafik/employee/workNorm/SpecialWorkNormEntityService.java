package online.stworzgrafik.StworzGrafik.employee.workNorm;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public interface SpecialWorkNormEntityService {
    SpecialWorkNorm getEntityById(@NotNull Long id);
}
