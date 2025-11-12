package online.stworzgrafik.StworzGrafik.employee;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.employee.DTO.CreateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.ResponseEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.UpdateEmployeeDTO;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface EmployeeService {
    public ResponseEmployeeDTO createEmployee(@NotNull Long storeId, @NotNull @Valid CreateEmployeeDTO createEmployeeDTO);
    public ResponseEmployeeDTO updateEmployee(@NotNull Long storeId, @NotNull Long employeeId, @NotNull @Valid UpdateEmployeeDTO updateEmployeeDTO);
    public void deleteEmployee(@NotNull Long storeId, @NotNull Long employeeId);
    public ResponseEmployeeDTO save(@NotNull Employee employee);
    public List<ResponseEmployeeDTO> findAll();
    public ResponseEmployeeDTO findById(@NotNull Long id);
    public boolean existsById(@NotNull Long id);
    public boolean existsBySap(@NotNull Long sap);
    public boolean existsByLastName(@NotNull String lastName);
}
