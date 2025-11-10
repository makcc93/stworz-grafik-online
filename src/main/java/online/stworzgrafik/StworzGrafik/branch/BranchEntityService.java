package online.stworzgrafik.StworzGrafik.branch;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public interface BranchEntityService {
    public Branch saveEntity(@NotNull Branch branch);
    public Branch getEntityById(@NotNull Long id);
}
