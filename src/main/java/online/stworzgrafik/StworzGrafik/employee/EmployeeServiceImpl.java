package online.stworzgrafik.StworzGrafik.employee;

import jakarta.annotation.Nullable;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.employee.DTO.CreateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.EmployeeSpecificationDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.ResponseEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.UpdateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.position.Position;
import online.stworzgrafik.StworzGrafik.employee.position.PositionEntityService;
import online.stworzgrafik.StworzGrafik.employee.position.PositionService;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import online.stworzgrafik.StworzGrafik.store.StoreService;
import online.stworzgrafik.StworzGrafik.validator.NameValidatorService;
import online.stworzgrafik.StworzGrafik.validator.ObjectType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static online.stworzgrafik.StworzGrafik.employee.EmployeeSpecification.*;

@Service
@Validated
@RequiredArgsConstructor
class EmployeeServiceImpl implements EmployeeService, EmployeeEntityService{
    private final EmployeeRepository employeeRepository;
    private final EmployeeBuilder employeeBuilder;
    private final EmployeeMapper employeeMapper;
    private final NameValidatorService nameValidatorService;
    private final StoreService storeService;
    private final StoreEntityService storeEntityService;
    private final PositionService positionService;
    private final PositionEntityService positionEntityService;
    private final UserAuthorizationService userAuthorizationService;

    @Override
    public ResponseEmployeeDTO createEmployee(Long storeId, CreateEmployeeDTO createEmployeeDTO) {
        if (employeeRepository.existsBySap(createEmployeeDTO.sap())){
            throw new EntityExistsException("Employee with sap " + createEmployeeDTO.sap() + " already exists");
        }

        String validatedFirstName = nameValidatorService.validate(createEmployeeDTO.firstName(), ObjectType.PERSON);
        String validatedLastName = nameValidatorService.validate(createEmployeeDTO.lastName(), ObjectType.PERSON);

        Store store = getStore(storeId);
        Position position = getPosition(createEmployeeDTO);

        Employee employee = employeeBuilder.createEmployee(
                validatedFirstName,
                validatedLastName,
                createEmployeeDTO.sap(),
                store,
                position
        );

        Employee savedEmployee = employeeRepository.save(employee);

        return employeeMapper.toResponseEmployeeDTO(savedEmployee);
    }

    @Override
    public ResponseEmployeeDTO updateEmployee(Long storeId, Long employeeId, UpdateEmployeeDTO updateEmployeeDTO) {
        Employee employee = getEmployeeIfBelongsToStore(storeId, employeeId);

        if (updateEmployeeDTO.firstName() != null){
            String validatedFirstName = nameValidatorService.validate(updateEmployeeDTO.firstName(),ObjectType.PERSON);

            employee.setFirstName(validatedFirstName);
        }

        if (updateEmployeeDTO.lastName() != null){
            String validatedLastName = nameValidatorService.validate(updateEmployeeDTO.lastName(),ObjectType.PERSON);

            employee.setFirstName(validatedLastName);
        }

        employeeMapper.updateEmployee(updateEmployeeDTO,employee);

        Employee savedEmployee = employeeRepository.save(employee);

        return employeeMapper.toResponseEmployeeDTO(savedEmployee);
    }

    @Override
    public void deleteEmployee(Long storeId, Long employeeId) {
        Employee employee = getEmployeeIfBelongsToStore(storeId, employeeId);

        employeeRepository.delete(employee);
    }

    @Override
    public ResponseEmployeeDTO save(Employee employee) {
        Employee savedEmployee = employeeRepository.save(employee);

        return employeeMapper.toResponseEmployeeDTO(savedEmployee);
    }

    @Override
    public List<ResponseEmployeeDTO> findAll() {
        return employeeRepository.findAll().stream()
                .map(employeeMapper::toResponseEmployeeDTO)
                .toList();
    }

    @Override
    public ResponseEmployeeDTO findById(Long storeId, Long employeeId) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find employee by id " + employeeId));

        return employeeMapper.toResponseEmployeeDTO(employee);
    }

    @Override
    public List<ResponseEmployeeDTO> findByCriteria(Long storeId, @Nullable EmployeeSpecificationDTO dto) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }

        Specification<Employee> specification = hasStoreId(storeId);

       if (dto != null) {
           specification =
                   specification.and(hasStoreId(storeId))
                           .and(hasId(dto.id()))
                           .and(hasFirstNameLike(dto.firstName()))
                           .and(hasLastNameLike(dto.lastName()))
                           .and(hasSap(dto.sap()))
                           .and(hasPositionId(dto.positionId()))
                           .and(isEnable(dto.enable()))
                           .and(canOperateCheckout(dto.canOperateCheckout()))
                           .and(canOperateCredit(dto.canOperateCredit()))
                           .and(canOpenCloseStore(dto.canOpenCloseStore()))
                           .and(isSeller(dto.seller()))
                           .and(isManager(dto.manager()));
       }

        return employeeRepository.findAll(specification).stream()
                .map(employeeMapper::toResponseEmployeeDTO)
                .toList();
    }

    @Override
    public boolean existsById(Long id) {
        return employeeRepository.existsById(id);
    }

    @Override
    public boolean existsBySap(Long sap) {
        return employeeRepository.existsBySap(sap);
    }

    @Override
    public boolean existsByLastName(String lastName) {
        return employeeRepository.existsByLastName(lastName);
    }

    @Override
    public Employee saveEntity(Employee employee){
        return employeeRepository.save(employee);
    }

    @Override
    public Employee getEntityById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find employee by id " + id));
    }

    private Employee getEmployeeIfBelongsToStore(Long storeId, Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find employee by id " + employeeId));

        if (!employee.getStore().getId().equals(storeId)){
            throw new AccessDeniedException("Employee does not belong to this store");
        }

        return employee;
    }

    private Store getStore(Long storeId){
        if (!storeService.existsById(storeId)){
            throw new EntityNotFoundException("Cannot find store by id " + storeId);
        }

        return storeEntityService.getEntityById(storeId);
    }

    private Position getPosition(CreateEmployeeDTO createEmployeeDTO){
        if (!positionService.exists(createEmployeeDTO.positionId())){
            throw new EntityNotFoundException("Cannot find position by id " + createEmployeeDTO.positionId());
        }

        return positionEntityService.getEntityById(createEmployeeDTO.positionId());
    }
}
