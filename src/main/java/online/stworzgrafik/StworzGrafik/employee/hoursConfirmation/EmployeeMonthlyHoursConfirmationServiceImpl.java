package online.stworzgrafik.StworzGrafik.employee.hoursConfirmation;

import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.calendar.CalendarCalculation;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.employee.hoursConfirmation.DTO.EmployeeHoursConfirmationDTO;
import online.stworzgrafik.StworzGrafik.employee.hoursConfirmation.DTO.SaveEmployeeHoursConfirmationRequest;
import online.stworzgrafik.StworzGrafik.employee.hoursConfirmation.DTO.UpdateEmployeeHoursConfirmationDTO;
import online.stworzgrafik.StworzGrafik.security.CurrentUserProvider;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import online.stworzgrafik.StworzGrafik.user.AppUser;
import online.stworzgrafik.StworzGrafik.user.label.UserLabelService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class EmployeeMonthlyHoursConfirmationServiceImpl implements EmployeeMonthlyHoursConfirmationService, EmployeeMonthlyHoursConfirmationEntityService {
    private final EmployeeMonthlyHoursConfirmationRepository repository;
    private final EmployeeMonthlyHoursConfirmationMapper mapper;
    private final UserAuthorizationService userAuthorizationService;
    private final StoreEntityService storeService;
    private final EmployeeEntityService employeeService;
    private final CurrentUserProvider currentUserProvider;
    private final UserLabelService userLabelService;
    private final CalendarCalculation calendarCalculation;

    @Override
    public List<EmployeeHoursConfirmationDTO> getHoursConfirmationForMonth(Long storeId, Integer year, Integer month) {
        verifyLoggedUserAccessToStore(storeId);

        // Magazynier ma osobny generator (WarehousemanScheduleGenerator) i nie liczy się
        // do wspólnego draftu godzinowego sklepu - isSpecial liczy się normalnie, bo mimo
        // że ma osobny matcher (SpecialEmployeesShiftMatcher), nadal konsumuje wspólny
        // budżet godzin rozliczeniowych.
        List<Employee> employees = employeeService.findAllStoreActiveEmployees(storeId).stream()
                .filter(employee -> !employee.isWarehouseman())
                .toList();

        Map<Long, EmployeeMonthlyHoursConfirmation> existingByEmployeeId = repository
                .findAllByStore_IdAndYearAndMonth(storeId, year, month).stream()
                .collect(Collectors.toMap(confirmation -> confirmation.getEmployee().getId(), confirmation -> confirmation));

        return employees.stream()
                .map(employee -> {
                    BigDecimal defaultNorm = calendarCalculation.getMonthlyNormForEmployee(year, month, employee);
                    Optional<EmployeeMonthlyHoursConfirmation> existing = Optional.ofNullable(existingByEmployeeId.get(employee.getId()));

                    return mapper.toResponseDTO(employee, defaultNorm, existing);
                })
                .toList();
    }

    @Override
    public List<EmployeeHoursConfirmationDTO> saveHoursConfirmation(Long storeId, Integer year, Integer month, SaveEmployeeHoursConfirmationRequest request) {
        verifyLoggedUserAccessToStore(storeId);

        Store store = storeService.getEntityById(storeId);
        AppUser currentUser = currentUserProvider.getCurrentUser();
        String label = userLabelService.buildLabel(currentUser);

        for (UpdateEmployeeHoursConfirmationDTO entry : request.confirmations()) {
            Employee employee = employeeService.getEntityById(entry.employeeId());

            if (!employee.getStore().getId().equals(store.getId())) {
                throw new AccessDeniedException("Employee with ID " + employee.getId() + " does not belong to store with ID " + store.getId());
            }

            EmployeeMonthlyHoursConfirmation confirmation = repository
                    .findByStore_IdAndEmployee_IdAndYearAndMonth(storeId, employee.getId(), year, month)
                    .orElse(null);

            if (confirmation == null) {
                confirmation = EmployeeMonthlyHoursConfirmation.builder()
                        .store(store)
                        .employee(employee)
                        .year(year)
                        .month(month)
                        .confirmedHours(entry.confirmedHours())
                        .createdByUserId(currentUser.getId())
                        .createdByLabel(label)
                        .build();
            } else {
                confirmation.setConfirmedHours(entry.confirmedHours());
                confirmation.setUpdatedByUserId(currentUser.getId());
                confirmation.setUpdatedByLabel(label);
            }

            repository.save(confirmation);
        }

        return getHoursConfirmationForMonth(storeId, year, month);
    }

    @Override
    public List<EmployeeMonthlyHoursConfirmation> getStoreMonthlyHoursConfirmations(Long storeId, Integer year, Integer month) {
        return repository.findAllByStore_IdAndYearAndMonth(storeId, year, month);
    }

    private void verifyLoggedUserAccessToStore(Long storeId) {
        if (!userAuthorizationService.hasAccessToStore(storeId)) {
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }
    }
}