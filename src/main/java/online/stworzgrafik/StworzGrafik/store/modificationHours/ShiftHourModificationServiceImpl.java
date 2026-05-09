package online.stworzgrafik.StworzGrafik.store.modificationHours;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ExcludedEmployeesRequest;
import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ShiftHourMappingRequest;
import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ShiftHourModificationConfigResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
class ShiftHourModificationServiceImpl implements ShiftHourModificationService {

    private final ShiftHourModificationConfigRepository repository;
    private final ShiftHourModificationConfigMapper mapper;
    private final StoreEntityService storeEntityService;
    private final EmployeeEntityService employeeEntityService;

    @Override
    @Transactional(readOnly = true)
    public ShiftHourModificationConfigResponse getHours(Long storeId) {
        ShiftHourModificationConfig config = findOrEmpty(storeId);
        return new ShiftHourModificationConfigResponse(
                mapper.toDto(config.getHoursToModify()),
                null
        );
    }

    @Override
    @Transactional
    public ShiftHourModificationConfigResponse updateHours(Long storeId, ShiftHourMappingRequest request) {
        ShiftHourModificationConfig config = findOrCreate(storeId);
        config.getHoursToModify().clear();
        config.getHoursToModify().putAll(mapper.toMap(request.hours()));
        repository.save(config);
        log.info("Updated shift hour mappings for store {}", storeId);
        return new ShiftHourModificationConfigResponse(
                mapper.toDto(config.getHoursToModify()),
                null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ShiftHourModificationConfigResponse getExcludedEmployees(Long storeId) {
        ShiftHourModificationConfig config = findOrEmpty(storeId);
        return new ShiftHourModificationConfigResponse(
                null,
                mapper.toEmployeeIds(config.getExcludedEmployees())
        );
    }

    @Override
    @Transactional
    public ShiftHourModificationConfigResponse updateExcludedEmployees(Long storeId, ExcludedEmployeesRequest request) {
        ShiftHourModificationConfig config = findOrCreate(storeId);

        List<Employee> excludedEmployees = employeeEntityService.findAllByIds(request.excludedEmployeeIds());
        if (excludedEmployees.size() != request.excludedEmployeeIds().size()) {
            log.warn("Some employee ids not found for store {}", storeId);
        }

        config.getExcludedEmployees().clear();
        config.getExcludedEmployees().addAll(excludedEmployees);
        repository.save(config);
        log.info("Updated excluded excludedEmployees for store {}", storeId);
        return new ShiftHourModificationConfigResponse(
                null,
                mapper.toEmployeeIds(config.getExcludedEmployees())
        );
    }

    // zwraca istniejący config lub nowy pusty obiekt (bez zapisu do DB)
    private ShiftHourModificationConfig findOrEmpty(Long storeId) {
        return repository.findByStoreId(storeId)
                .orElseGet(() -> ShiftHourModificationConfig.builder()
                        .build());
    }

    private ShiftHourModificationConfig findOrCreate(Long storeId) {
        return repository.findByStoreId(storeId)
                .orElseGet(() -> {
                    Store store = storeEntityService.getEntityById(storeId);
                    return ShiftHourModificationConfig.builder()
                            .store(store)
                            .build();
                });
    }
}
