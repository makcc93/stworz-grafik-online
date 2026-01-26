package online.stworzgrafik.StworzGrafik.employee;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.employee.DTO.CreateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.EmployeeSpecificationDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.ResponseEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.UpdateEmployeeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
 public interface EmployeeService {
     ResponseEmployeeDTO createEmployee(@NotNull Long storeId, @NotNull @Valid CreateEmployeeDTO createEmployeeDTO);
     ResponseEmployeeDTO updateEmployee(@NotNull Long storeId, @NotNull Long employeeId, @NotNull @Valid UpdateEmployeeDTO updateEmployeeDTO);
     void deleteEmployee(@NotNull Long storeId, @NotNull Long employeeId);
     ResponseEmployeeDTO save(@NotNull Employee employee);
     List<ResponseEmployeeDTO> findAll();
     ResponseEmployeeDTO findById(@NotNull Long storeId, @NotNull Long employeeId);
     Page<ResponseEmployeeDTO> findByCriteria(@NotNull Long storeId, @Nullable EmployeeSpecificationDTO dto, Pageable pageable);
     boolean existsById(@NotNull Long id);
     boolean existsBySap(@NotNull Long sap);
     boolean existsByLastName(@NotNull String lastName);
}
