package online.stworzgrafik.StworzGrafik.store.modificationHours;

import jakarta.persistence.EntityExistsException;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
class ShiftHourModificationServiceImpl implements ShiftHourModificationService {

    private final ShiftHourModificationConfigRepository repository;
    private final ShiftHourModificationConfigMapper mapper;
    private final StoreEntityService storeEntityService;
    private final EmployeeEntityService employeeEntityService;

    @Override
    @Transactional
    public ShiftHourModificationConfigResponse create(Long storeId, ShiftHourMappingRequest request, ExcludedEmployeesRequest employeesRequest) {
        Optional<ShiftHourModificationConfig> existing = repository.findByStoreId(storeId);
        if (existing.isPresent()) {
            throw new EntityExistsException("Shift modification hours for store with id " + storeId + " already exist");
        }

        List<ShiftHourRule> rules = new ArrayList<>(mapper.toRules(request.hours()));
        validateRules(rules);

        List<Employee> excludedEmployees = new ArrayList<>();
        for (Long employeeId : employeesRequest.excludedEmployeeIds()) {
            excludedEmployees.add(employeeEntityService.getEntityById(employeeId));
        }

        Store store = storeEntityService.getEntityById(storeId);

        ShiftHourModificationConfig config = ShiftHourModificationConfig.builder()
                .store(store)
                .rules(rules)
                .excludedEmployees(excludedEmployees)
                .build();

        repository.save(config);

        return new ShiftHourModificationConfigResponse(mapper.toDto(rules), mapper.toEmployeeIds(excludedEmployees));
    }

    @Override
    @Transactional(readOnly = true)
    public ShiftHourModificationConfigResponse getHours(Long storeId) {
        ShiftHourModificationConfig config = findOrEmpty(storeId);
        return new ShiftHourModificationConfigResponse(
                mapper.toDto(config.getRules()),
                null
        );
    }

    @Override
    @Transactional
    public ShiftHourModificationConfigResponse updateHours(Long storeId, ShiftHourMappingRequest request) {
        List<ShiftHourRule> rules = mapper.toRules(request.hours());
        validateRules(rules);

        ShiftHourModificationConfig config = findOrCreate(storeId);
        config.getRules().clear();
        config.getRules().addAll(rules);
        repository.save(config);

        return new ShiftHourModificationConfigResponse(
                mapper.toDto(config.getRules()),
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
    @Transactional
    public void delete(Long storeId) {
        Optional<ShiftHourModificationConfig> optionalShiftHourModificationConfig = repository.findByStoreId(storeId);
        if (optionalShiftHourModificationConfig.isEmpty()) throw new EntityNotFoundException("Cannot find shift hour modification config for store with id " + storeId);

        repository.delete(optionalShiftHourModificationConfig.get());
    }

    private void validateRules(List<ShiftHourRule> rules) {
        for (int i = 0; i < rules.size(); i++) {
            for (int j = i + 1; j < rules.size(); j++) {
                ShiftHourRule first = rules.get(i);
                ShiftHourRule second = rules.get(j);

                if (first.getOriginalHour().equals(second.getOriginalHour())
                        && first.getScope().overlaps(second.getScope())) {
                    throw new IllegalArgumentException(
                            "Overlapping shift hour rules for hour " + first.getOriginalHour()
                                    + " (" + first.getScope() + " and " + second.getScope() + ")"
                    );
                }
            }
        }
    }

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