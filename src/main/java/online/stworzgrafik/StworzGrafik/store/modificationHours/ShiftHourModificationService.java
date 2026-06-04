package online.stworzgrafik.StworzGrafik.store.modificationHours;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ExcludedEmployeesRequest;
import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ShiftHourMappingRequest;
import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ShiftHourModificationConfigResponse;
import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ShiftHourModificationDTO;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface ShiftHourModificationService {
    ShiftHourModificationConfigResponse create(@NotNull Long storeId, @Valid ShiftHourMappingRequest request, ExcludedEmployeesRequest employeesRequest);
    ShiftHourModificationConfigResponse getHours(@NotNull Long storeId);
    ShiftHourModificationConfigResponse updateHours(@NotNull Long storeId, @Valid ShiftHourMappingRequest request);
    ShiftHourModificationConfigResponse getExcludedEmployees(@NotNull Long storeId);
    ShiftHourModificationConfigResponse updateExcludedEmployees(@NotNull Long storeId, @Valid ExcludedEmployeesRequest request);
    void delete(@NotNull Long storeId);
}
