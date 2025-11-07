package online.stworzgrafik.StworzGrafik.employee;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import online.stworzgrafik.StworzGrafik.employee.DTO.CreateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.ResponseEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.UpdateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.position.Position;
import online.stworzgrafik.StworzGrafik.employee.position.PositionService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreService;
import online.stworzgrafik.StworzGrafik.validator.NameValidatorService;
import online.stworzgrafik.StworzGrafik.validator.ObjectType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@AllArgsConstructor
public class EmployeeService{
    private final EmployeeRepository employeeRepository;
    private final EmployeeBuilder employeeBuilder;
    private final NameValidatorService nameValidatorService;
    private final EmployeeMapper employeeMapper;
    private final StoreService storeService;
    private final PositionService positionService;
    private final EntityManager entityManager;

    public ResponseEmployeeDTO createEmployee(@Valid Long storeId, @Valid CreateEmployeeDTO createEmployeeDTO) {
        if (employeeRepository.existsBySap(createEmployeeDTO.sap())){
            throw new EntityExistsException("Employee with sap " + createEmployeeDTO.sap() + " already exists");
        }

        String validatedFirstName = nameValidatorService.validate(createEmployeeDTO.firstName(), ObjectType.PERSON);
        String validatedLastName = nameValidatorService.validate(createEmployeeDTO.lastName(), ObjectType.PERSON);

        Store store = getStoreReference(storeId);
        Position position = getPositionReference(createEmployeeDTO);

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

    public ResponseEmployeeDTO updateEmployee(@Valid Long storeId, @Valid Long employeeId, @Valid UpdateEmployeeDTO updateEmployeeDTO) {
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

    public Employee save(@Valid Employee employee){
        return employeeRepository.save(employee);
    }

    public void deleteEmployee(@Valid Long storeId, @Valid Long employeeId) {
        Employee employee = getEmployeeIfBelongsToStore(storeId, employeeId);

        employeeRepository.delete(employee);
    }

    public List<ResponseEmployeeDTO> findAll() {
        return employeeRepository.findAll().stream()
                .map(employeeMapper::toResponseEmployeeDTO)
                .toList();
    }

    public ResponseEmployeeDTO findById(@Valid Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find employee by id " + id));

        return employeeMapper.toResponseEmployeeDTO(employee);
    }

    public boolean existsById(@Valid Long id) {
        return employeeRepository.existsById(id);
    }

    public boolean existsBySap(@Valid Long sap) {
        return employeeRepository.existsBySap(sap);
    }

    public boolean existsByLastName(@Valid String lastName) {
        return employeeRepository.existsByLastName(lastName);
    }

    private Employee getEmployeeIfBelongsToStore(@Valid Long storeId, @Valid Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find employee by id " + employeeId));

        if (!employee.getStore().getId().equals(storeId)){
            throw new AccessDeniedException("Employee does not belong to this store");
        }

        return employee;
    }

    private Store getStoreReference(@Valid Long storeId){
        if (!storeService.exists(storeId)){
            throw new EntityNotFoundException("Cannot find store by id " + storeId);
        }

        return entityManager.getReference(Store.class,storeId);
    }

    private Position getPositionReference(@Valid CreateEmployeeDTO createEmployeeDTO){
        if (!positionService.exists(createEmployeeDTO.positionId())){
            throw new EntityNotFoundException("Cannot find position by id " + createEmployeeDTO.positionId());
        }

        return entityManager.getReference(Position.class,createEmployeeDTO.positionId());
    }
}
