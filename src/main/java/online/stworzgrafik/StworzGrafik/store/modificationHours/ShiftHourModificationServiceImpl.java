package online.stworzgrafik.StworzGrafik.store.modificationHours;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ExcludedEmployeesRequest;
import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ShiftHourMappingRequest;
import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ShiftHourModificationConfigResponse;
import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ShiftHourModificationDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
class ShiftHourModificationServiceImpl implements ShiftHourModificationService {

    private final ShiftHourModificationConfigRepository repository;
    private final ShiftHourModificationConfigMapper mapper;
    private final StoreEntityService storeEntityService;
    private final EmployeeEntityService employeeEntityService;

    @Override
    public ShiftHourModificationConfigResponse create(Long storeId, ShiftHourMappingRequest request, ExcludedEmployeesRequest employeesRequest) {
        List<@Valid ShiftHourModificationDTO> hours = request.hours();

        List<Employee> excludedEmployees = new ArrayList<>();
        for (Long employeeId : employeesRequest.excludedEmployeeIds()) {
            excludedEmployees.add(employeeEntityService.getEntityById(employeeId));
        }

        Optional<ShiftHourModificationConfig> optionalShiftHourModificationConfig = repository.findByStoreId(storeId);
        if (optionalShiftHourModificationConfig.isPresent()) throw new EntityExistsException("Shift modification hours for store with id " + storeId + " already exist");

        HashMap<@NotNull LocalTime, @NotNull LocalTime> hoursToModify = hours.stream()
                .collect(Collectors.toMap(
                        ShiftHourModificationDTO::originalHour,
                        ShiftHourModificationDTO::modifiedHour,
                        (h1, h2) -> h1,
                        HashMap::new
                ));
        ShiftHourModificationConfig shiftHourModificationConfig = ShiftHourModificationConfig.builder()
                .hoursToModify(hoursToModify)
                .excludedEmployees(excludedEmployees)
                .build();

        repository.save(shiftHourModificationConfig);

        return new ShiftHourModificationConfigResponse(mapper.toDto(hoursToModify),mapper.toEmployeeIds(excludedEmployees));
    }

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
        return new ShiftHourModificationConfigResponse(
                null,
                mapper.toEmployeeIds(config.getExcludedEmployees())
        );
    }

    @Override
    public void delete(Long storeId) {
        Optional<ShiftHourModificationConfig> optionalShiftHourModificationConfig = repository.findByStoreId(storeId);
        if (optionalShiftHourModificationConfig.isEmpty()) throw new EntityNotFoundException("Cannot find shift hour modification config for store with id " + storeId);

        repository.delete(optionalShiftHourModificationConfig.get());
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
