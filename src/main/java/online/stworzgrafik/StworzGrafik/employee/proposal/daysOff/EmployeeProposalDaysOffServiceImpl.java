package online.stworzgrafik.StworzGrafik.employee.proposal.daysOff;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.CreateEmployeeProposalDaysOffDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.EmployeeProposalDaysOffSpecificationDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.ResponseEmployeeProposalDaysOffDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.UpdateEmployeeProposalDaysOffDTO;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import static online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.EmployeeProposalDaysOffSpecification.*;

@Service
@RequiredArgsConstructor
class EmployeeProposalDaysOffServiceImpl implements EmployeeProposalDaysOffService{
    private final EmployeeProposalDaysOffRepository repository;
    private final EmployeeProposalDaysOffMapper mapper;
    private final EmployeeProposalDaysOffBuilder builder;
    private final UserAuthorizationService userAuthorizationService;
    private final StoreEntityService storeService;
    private final EmployeeEntityService employeeService;

    @Override
    public ResponseEmployeeProposalDaysOffDTO createEmployeeProposalDaysOff(Long storeId,
                                                                            Long employeeId,
                                                                            CreateEmployeeProposalDaysOffDTO dto) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }

        Store store = storeService.getEntityById(storeId);

        Employee employee = employeeService.getEntityById(employeeId);

        if (!employee.getStore().equals(store)){
            throw new AccessDeniedException("Employee with ID " + employee.getId() + " does not belong to store with ID " + store.getId());
        }

        if (repository.existsByStore_IdAndEmployee_IdAndYearAndMonth(storeId, employeeId, dto.year(), dto.month())){
            throw new EntityExistsException("Employee proposal days off in month " + dto.month() + " of  year " + dto.year() + " already exists");
        }

        EmployeeProposalDaysOff employeeProposalDaysOff = builder.createEmployeeProposalDaysOff(
                store,
                employee,
                dto.year(),
                dto.month(),
                dto.monthlyDaysOff()
        );

        EmployeeProposalDaysOff saved = repository.save(employeeProposalDaysOff);

        return mapper.toResponseEmployeeProposalDaysOffDTO(saved);
    }

    @Override
    public ResponseEmployeeProposalDaysOffDTO updateEmployeeProposalDaysOff(Long storeId,
                                                                            Long employeeId,
                                                                            Long employeeProposalDaysOffId,
                                                                            UpdateEmployeeProposalDaysOffDTO dto) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }

        EmployeeProposalDaysOff employeeProposalDaysOff = repository.findById(employeeProposalDaysOffId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find employee proposal days off with id " + employeeProposalDaysOffId));

        Store store = storeService.getEntityById(storeId);

        Employee employee = employeeService.getEntityById(employeeId);

        if (!employee.getStore().equals(store)){
            throw new AccessDeniedException("Employee with ID " + employee.getId() + " does not belong to store with ID " + store.getId());
        }

        mapper.updateEmployeeProposalDaysOff(dto,employeeProposalDaysOff);

        EmployeeProposalDaysOff saved = repository.save(employeeProposalDaysOff);

        return mapper.toResponseEmployeeProposalDaysOffDTO(saved);
    }

    @Override
    public ResponseEmployeeProposalDaysOffDTO save(EmployeeProposalDaysOff employeeProposalDaysOff) {
        EmployeeProposalDaysOff saved = repository.save(employeeProposalDaysOff);

        return mapper.toResponseEmployeeProposalDaysOffDTO(saved);
    }

    @Override
    public void delete(Long storeId,
                       Long employeeId,
                       Long employeeProposalDaysOffId) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }

        EmployeeProposalDaysOff employeeProposalDaysOff = repository.findById(employeeProposalDaysOffId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find employee proposal days off by id " + employeeProposalDaysOffId));

        repository.delete(employeeProposalDaysOff);
    }

    @Override
    public ResponseEmployeeProposalDaysOffDTO getById(Long storeId,
                                                      Long employeeId,
                                                      Long employeeProposalDaysOffId) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }

        EmployeeProposalDaysOff employeeProposalDaysOff = repository.findById(employeeProposalDaysOffId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find employee proposal days off by id " + employeeProposalDaysOffId));

        return mapper.toResponseEmployeeProposalDaysOffDTO(employeeProposalDaysOff);
    }

    @Override
    public Page<ResponseEmployeeProposalDaysOffDTO> getByCriteria(Long storeId, EmployeeProposalDaysOffSpecificationDTO dto, Pageable pageable) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }

        Specification<EmployeeProposalDaysOff> specification = Specification.allOf(
                hasStoreId(storeId),
                hasEmployeeId(dto.employeeId()),
                hasYear(dto.year()),
                hasMonth(dto.month())
        );

        return repository.findAll(specification,pageable)
                .map(mapper::toResponseEmployeeProposalDaysOffDTO);
    }

    @Override
    public boolean exists(Long employeeProposalDaysOffId) {
        return repository.existsById(employeeProposalDaysOffId);
    }
}
