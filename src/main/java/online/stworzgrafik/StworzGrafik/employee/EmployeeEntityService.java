package online.stworzgrafik.StworzGrafik.employee;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.employee.DTO.EmployeeSpecificationDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.ResponseEmployeeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

@Validated
public interface EmployeeEntityService {
   Employee saveEntity(@NotNull Employee employee);
   Employee getEntityById(@NotNull Long id);
   Page<Employee> findEntityByCriteria(@NotNull Long storeId, @Nullable EmployeeSpecificationDTO dto, Pageable pageable);
}
