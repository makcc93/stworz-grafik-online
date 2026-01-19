package online.stworzgrafik.StworzGrafik.employee.vacation;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.CreateEmployeeVacationDTO;
import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.EmployeeVacationSpecificationDTO;
import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.ResponseEmployeeVacationDTO;
import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.UpdateEmployeeVacationDTO;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

import static online.stworzgrafik.StworzGrafik.employee.vacation.EmployeeVacationSpecification.*;


@Service
@RequiredArgsConstructor
class EmployeeVacationServiceImpl implements EmployeeVacationService{
    private final EmployeeVacationRepository repository;
    private final EmployeeVacationMapper mapper;
    private final EmployeeVacationBuilder builder;
    private final UserAuthorizationService userAuthorizationService;
    private final StoreEntityService storeService;
    private final EmployeeEntityService employeeService;

    @Override
    public ResponseEmployeeVacationDTO createEmployeeProposalVacation(Long storeId,
                                                                      Long employeeId,
                                                                      CreateEmployeeVacationDTO dto) {
        if (!userAuthorizationService.hasAccessToStore(storeId)) {
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }

        Store store = storeService.getEntityById(storeId);

        Employee employee = employeeService.getEntityById(employeeId);

        if (!employee.getStore().equals(store)) {
            throw new AccessDeniedException("Employee with ID " + employee.getId() + " does not belong to store with ID " + store.getId());
        }

        if (repository.existsByStore_IdAndEmployee_IdAndYearAndMonth(storeId, employeeId, dto.year(), dto.month())) {
            throw new EntityExistsException("Employee vacation in month " + dto.month() + " of  year " + dto.year() + " already exists");
        }

        EmployeeVacation employeeVacation = builder.createEmployeeVacation(
                store,
                employee,
                dto.year(),
                dto.month(),
                dto.monthlyVacation()
        );

        EmployeeVacation saved = repository.save(employeeVacation);

        return mapper.toResponseEmployeeVacationDTO(saved);
    }

    @Override
    public ResponseEmployeeVacationDTO updateEmployeeVacation(Long storeId,
                                                              Long employeeId,
                                                              Long employeeVacationId,
                                                              UpdateEmployeeVacationDTO dto) {
        if (!userAuthorizationService.hasAccessToStore(storeId)) {
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }

        EmployeeVacation employeeVacation = repository.findById(employeeVacationId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find employee vacation with id " + employeeVacationId));

        Store store = storeService.getEntityById(storeId);

        Employee employee = employeeService.getEntityById(employeeId);

        if (!employee.getStore().equals(store)) {
            throw new AccessDeniedException("Employee with ID " + employee.getId() + " does not belong to store with ID " + store.getId());
        }

        mapper.updateEmployeeVacation(dto, employeeVacation);

        EmployeeVacation saved = repository.save(employeeVacation);

        return mapper.toResponseEmployeeVacationDTO(saved);
    }

    @Override
    public ResponseEmployeeVacationDTO save(EmployeeVacation employeeVacation) {
        EmployeeVacation saved = repository.save(employeeVacation);

        return mapper.toResponseEmployeeVacationDTO(saved);
    }

    @Override
    public void delete(Long storeId,
                       Long employeeId,
                       Long employeeVacationId) {
        if (!userAuthorizationService.hasAccessToStore(storeId)) {
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }

        EmployeeVacation employeeVacation = repository.findById(employeeVacationId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find employee vacation by id " + employeeVacationId));

        repository.delete(employeeVacation);
    }

    @Override
    public ResponseEmployeeVacationDTO getById(Long storeId,
                                               Long employeeId,
                                               Long employeeVacationId) {
        if (!userAuthorizationService.hasAccessToStore(storeId)) {
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }

        EmployeeVacation employeeVacation = repository.findById(employeeVacationId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find employee vacation by id " + employeeVacationId));

        return mapper.toResponseEmployeeVacationDTO(employeeVacation);
    }

    @Override
    public List<ResponseEmployeeVacationDTO> getByCriteria(Long storeId, EmployeeVacationSpecificationDTO dto) {
        if (!userAuthorizationService.hasAccessToStore(storeId)) {
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }

        Specification<EmployeeVacation> specification = Specification.allOf(
                hasStoreId(storeId),
                hasEmployeeId(dto.employeeId()),
                hasYear(dto.year()),
                hasMonth(dto.month())
        );

        return repository.findAll(specification).stream()
                .map(mapper::toResponseEmployeeVacationDTO)
                .toList();
    }

    @Override
    public boolean exists(Long employeeVacationId) {
        return repository.existsById(employeeVacationId);
    }
}
