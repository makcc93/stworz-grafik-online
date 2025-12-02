package online.stworzgrafik.StworzGrafik.branch;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public interface BranchEntityService {
     Branch saveEntity(@NotNull Branch branch);
     Branch getEntityById(@NotNull Long id);
}
