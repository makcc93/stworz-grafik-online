package online.stworzgrafik.StworzGrafik.employee;

import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.employee.DTO.CreateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.ResponseEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.UpdateEmployeeDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface EmployeeService {
    ResponseEmployeeDTO createEmployee(@NotNull CreateEmployeeDTO createEmployeeDTO);
    ResponseEmployeeDTO update(@NotNull UpdateEmployeeDTO updateEmployeeDTO);
    void deleteEmployee(@NotNull Long id);
    List<ResponseEmployeeDTO> findAll();
    ResponseEmployeeDTO findById(@NotNull Long id);
    boolean existsById(@NotNull Long id);
    boolean existsBySap(@NotNull Long sap);
    boolean existsByLastName(@NotNull String lastName);
}
