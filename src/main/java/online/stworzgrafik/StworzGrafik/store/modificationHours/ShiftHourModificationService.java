package online.stworzgrafik.StworzGrafik.store.modificationHours;

import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ExcludedEmployeesRequest;
import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ShiftHourMappingRequest;
import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ShiftHourModificationConfigResponse;
import org.springframework.validation.annotation.Validated;

@Validated
public interface ShiftHourModificationService {
    ShiftHourModificationConfigResponse getHours(Long storeId);
    ShiftHourModificationConfigResponse updateHours(Long storeId, ShiftHourMappingRequest request);
    ShiftHourModificationConfigResponse getExcludedEmployees(Long storeId);
    ShiftHourModificationConfigResponse updateExcludedEmployees(Long storeId, ExcludedEmployeesRequest request);
}
