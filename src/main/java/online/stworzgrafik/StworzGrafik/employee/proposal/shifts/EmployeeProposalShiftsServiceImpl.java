package online.stworzgrafik.StworzGrafik.employee.proposal.shifts;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO.CreateEmployeeProposalShiftsDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO.ResponseEmployeeProposalShiftsDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO.UpdateEmployeeProposalShiftsDTO;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static online.stworzgrafik.StworzGrafik.employee.proposal.shifts.EmployeeProposalShiftsSpecification.*;

@Service
@RequiredArgsConstructor
class EmployeeProposalShiftsServiceImpl implements EmployeeProposalShiftsService{
    private final EmployeeProposalShiftsRepository repository;
    private final EmployeeProposalShiftsMapper mapper;
    private final EmployeeProposalShiftsBuilder builder;
    private final UserAuthorizationService userAuthorizationService;
    private final StoreEntityService storeService;
    private final EmployeeEntityService employeeService;

    @Override
    public ResponseEmployeeProposalShiftsDTO createEmployeeProposalShift(Long storeId, Long employeeId, CreateEmployeeProposalShiftsDTO dto) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }

        Store store = storeService.getEntityById(storeId);

        Employee employee = employeeService.getEntityById(employeeId);

        if (!employee.getStore().equals(store)){
            throw new AccessDeniedException("Employee with ID " + employee.getId() + " does not belong to store with ID " + store.getId());
        }

        if (repository.existsByStore_IdAndEmployee_IdAndDate(storeId,employeeId,dto.date())){
            throw new EntityExistsException("Employee with ID " + employeeId + " proposal shift for date " + dto.date() + " already exists");
        }

        EmployeeProposalShifts employeeProposalShifts = builder.createEmployeeProposalShifts(store, employee, dto.date(), dto.dailyProposalShift());

        EmployeeProposalShifts saved = repository.save(employeeProposalShifts);

        return mapper.toResponseEmployeeProposalShiftsDTO(saved);
    }

    @Override
    public ResponseEmployeeProposalShiftsDTO updateEmployeeProposalShift(Long storeId, Long employeeId, Long employeeProposalShiftId, UpdateEmployeeProposalShiftsDTO dto) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }

        EmployeeProposalShifts employeeProposalShifts = repository.findById(employeeProposalShiftId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find employee proposal shift by id " + employeeProposalShiftId));

        Store store = storeService.getEntityById(storeId);

        Employee employee = employeeService.getEntityById(employeeId);

        if (!employee.getStore().equals(store)){
            throw new AccessDeniedException("Employee with ID " + employee.getId() + " does not belong to store with ID " + store.getId());
        }

        mapper.updateEmployeeProposalShifts(dto,employeeProposalShifts);

        EmployeeProposalShifts saved = repository.save(employeeProposalShifts);

        return mapper.toResponseEmployeeProposalShiftsDTO(saved);
    }

    @Override
    public ResponseEmployeeProposalShiftsDTO save(EmployeeProposalShifts employeeProposalShifts) {
        EmployeeProposalShifts saved = repository.save(employeeProposalShifts);

        return mapper.toResponseEmployeeProposalShiftsDTO(saved);
    }

    @Override
    public void delete(Long storeId, Long employeeId, Long employeeProposalShiftId) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }

        EmployeeProposalShifts employeeProposalShifts = repository.findById(employeeProposalShiftId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find employee proposal shift by id " + employeeProposalShiftId));

        repository.delete(employeeProposalShifts);
    }

    @Override
    public ResponseEmployeeProposalShiftsDTO getById(Long storeId, Long employeeId, Long employeeProposalShiftId) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }

        EmployeeProposalShifts employeeProposalShifts = repository.findById(employeeProposalShiftId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find employee proposal shift by id " + employeeProposalShiftId));

        return mapper.toResponseEmployeeProposalShiftsDTO(employeeProposalShifts);
    }

    @Override
    public List<ResponseEmployeeProposalShiftsDTO> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponseEmployeeProposalShiftsDTO)
                .toList();
    }

    @Override
    public boolean exists(Long employeeProposalShiftId) {
        return repository.existsById(employeeProposalShiftId);
    }

    @Override
    public List<ResponseEmployeeProposalShiftsDTO> getByCriteria(Long storeId, Long employeeId, LocalDate startDate, LocalDate endDate) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }

        if (startDate == null && endDate != null){
            throw new IllegalArgumentException("Start date is required when end date is provided");
        }

        Specification<EmployeeProposalShifts> specification = Specification.allOf(
                hasStoreId(storeId),
                hasEmployeeId(employeeId),
                isBetweenDates(startDate,endDate)
        );

        return repository.findAll(specification).stream()
                .map(mapper::toResponseEmployeeProposalShiftsDTO)
                .toList();
    }
}
