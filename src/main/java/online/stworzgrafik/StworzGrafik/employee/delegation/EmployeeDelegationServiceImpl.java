package online.stworzgrafik.StworzGrafik.employee.delegation;

import jakarta.annotation.Nullable;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.employee.delegation.DTO.CreateEmployeeDelegationDTO;
import online.stworzgrafik.StworzGrafik.employee.delegation.DTO.EmployeeDelegationSpecificationDTO;
import online.stworzgrafik.StworzGrafik.employee.delegation.DTO.ResponseEmployeeDelegationDTO;
import online.stworzgrafik.StworzGrafik.employee.delegation.DTO.UpdateEmployeeDelegationDTO;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

import static online.stworzgrafik.StworzGrafik.employee.delegation.EmployeeDelegationSpecification.*;

@Service
@RequiredArgsConstructor
class EmployeeDelegationServiceImpl implements EmployeeDelegationService, EmployeeDelegationEntityService{
    private final EmployeeDelegationRepository repository;
    private final EmployeeDelegationMapper mapper;
    private final EmployeeDelegationBuilder builder;
    private final UserAuthorizationService userAuthorizationService;
    private final StoreEntityService storeService;
    private final EmployeeEntityService employeeService;

    @Override
    public ResponseEmployeeDelegationDTO createEmployeeProposalDelegation(Long storeId,
                                                                        Long employeeId,
                                                                        CreateEmployeeDelegationDTO dto) {
        verifyLoggedUserAccessToStore(storeId);

        Store store = storeService.getEntityById(storeId);

        Employee employee = employeeService.getEntityById(employeeId);

//        if (!employee.getStore().equals(store)) {
//            throw new AccessDeniedException("Employee with ID " + employee.getId() + " does not belong to store with ID " + store.getId());
//        }

        if (repository.existsByStore_IdAndEmployee_IdAndYearAndMonth(storeId, employeeId, dto.year(), dto.month())) {
            throw new EntityExistsException("Employee delegation in month " + dto.month() + " of  year " + dto.year() + " already exists");
        }

        EmployeeDelegation employeeDelegation = builder.createEmployeeDelegation(
                store,
                employee,
                dto.year(),
                dto.month(),
                dto.monthlyDelegation()
        );

        EmployeeDelegation saved = repository.save(employeeDelegation);

        return mapper.toResponseEmployeeDelegationDTO(saved);
    }

    @Override
    public ResponseEmployeeDelegationDTO updateEmployeeDelegation(Long storeId,
                                                              Long employeeId,
                                                              Long employeeDelegationId,
                                                              UpdateEmployeeDelegationDTO dto) {
        verifyLoggedUserAccessToStore(storeId);

        EmployeeDelegation employeeDelegation = repository.findById(employeeDelegationId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find employee delegation with id " + employeeDelegationId));

//        Store store = storeService.getEntityById(storeId);
//
//        Employee employee = employeeService.getEntityById(employeeId);
//
//        if (!employee.getStore().equals(store)) {
//            throw new AccessDeniedException("Employee with ID " + employee.getId() + " does not belong to store with ID " + store.getId());
//        }

        mapper.updateEmployeeDelegation(dto, employeeDelegation);

        EmployeeDelegation saved = repository.save(employeeDelegation);

        return mapper.toResponseEmployeeDelegationDTO(saved);
    }

    @Override
    public ResponseEmployeeDelegationDTO save(EmployeeDelegation employeeDelegation) {
        EmployeeDelegation saved = repository.save(employeeDelegation);

        return mapper.toResponseEmployeeDelegationDTO(saved);
    }

    @Override
    public void delete(Long storeId,
                       Long employeeId,
                       Long employeeDelegationId) {
        verifyLoggedUserAccessToStore(storeId);

        EmployeeDelegation employeeVacation = repository.findById(employeeDelegationId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find employee delegation by id " + employeeDelegationId));

        repository.delete(employeeVacation);
    }

    @Override
    public ResponseEmployeeDelegationDTO getById(Long storeId,
                                               Long employeeId,
                                               Long employeeDelegationId) {
        verifyLoggedUserAccessToStore(storeId);

        EmployeeDelegation employeeDelegation = repository.findById(employeeDelegationId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find employee delegation by id " + employeeDelegationId));

        return mapper.toResponseEmployeeDelegationDTO(employeeDelegation);
    }

    @Override
    public Page<ResponseEmployeeDelegationDTO> getByCriteria(@Nullable Long storeId, EmployeeDelegationSpecificationDTO dto, Pageable pageable) {
        verifyLoggedUserAccessToStore(storeId);

        Specification<EmployeeDelegation> specification = Specification.allOf(
                hasStoreId(storeId),
                hasEmployeeId(dto.employeeId()),
                hasYear(dto.year()),
                hasMonth(dto.month())
        );

        return repository.findAll(specification,pageable)
                .map(mapper::toResponseEmployeeDelegationDTO);
    }

    @Override
    public boolean exists(Long employeeDelegationId) {
        return repository.existsById(employeeDelegationId);
    }

    @Override
    public List<EmployeeDelegation> getEmployeeMonthlyDelegation(Long storeId, Integer year, Integer month) {
        verifyLoggedUserAccessToStore(storeId);

        return repository.findAllByStore_IdAndYearAndMonth(storeId,year,month);
    }

    private void verifyLoggedUserAccessToStore(Long storeId) {
        if (!userAuthorizationService.hasAccessToStore(storeId)) {
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }
    }
}
