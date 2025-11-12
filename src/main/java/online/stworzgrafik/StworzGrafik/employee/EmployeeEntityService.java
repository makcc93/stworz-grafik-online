package online.stworzgrafik.StworzGrafik.employee;

import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public interface EmployeeEntityService {
    public Employee saveEntity(@NotNull Employee employee);
    public Employee getEntityById(@NotNull Long id);
}
