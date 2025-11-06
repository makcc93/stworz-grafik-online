package online.stworzgrafik.StworzGrafik.employee;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.employee.DTO.CreateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.ResponseEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.UpdateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.position.Position;
import online.stworzgrafik.StworzGrafik.employee.position.PositionRepository;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreRepository;
import online.stworzgrafik.StworzGrafik.validator.NameValidatorService;
import online.stworzgrafik.StworzGrafik.validator.ObjectType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;

@Service
@Validated
public class EmployeeService{
    private final EmployeeRepository employeeRepository;
    private final EmployeeBuilder employeeBuilder;
    private final NameValidatorService nameValidatorService;
    private final EmployeeMapper employeeMapper;
    private final StoreRepository storeRepository;
    private final PositionRepository positionRepository;

    public EmployeeService(EmployeeRepository employeeRepository, EmployeeBuilder employeeBuilder, NameValidatorService nameValidatorService, EmployeeMapper employeeMapper, StoreRepository storeRepository, PositionRepository positionRepository) {
        this.employeeRepository = employeeRepository;
        this.employeeBuilder = employeeBuilder;
        this.nameValidatorService = nameValidatorService;
        this.employeeMapper = employeeMapper;
        this.storeRepository = storeRepository;
        this.positionRepository = positionRepository;
    }

    public ResponseEmployeeDTO createEmployee(Long storeId, CreateEmployeeDTO createEmployeeDTO) {
        Objects.requireNonNull(storeId, "Store id cannot be null");
        Objects.requireNonNull(createEmployeeDTO);

        if (employeeRepository.existsBySap(createEmployeeDTO.sap())){
            throw new EntityExistsException("Employee with sap " + createEmployeeDTO.sap() + " already exists");
        }

        String validatedFirstName = nameValidatorService.validate(createEmployeeDTO.firstName(), ObjectType.PERSON);
        String validatedLastName = nameValidatorService.validate(createEmployeeDTO.lastName(), ObjectType.PERSON);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find store by id " + storeId));
        Position position = positionRepository.findById(createEmployeeDTO.positionId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find position by id " + createEmployeeDTO.positionId()));

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

    public ResponseEmployeeDTO updateEmployee(Long storeId, Long employeeId, UpdateEmployeeDTO updateEmployeeDTO) {
        Objects.requireNonNull(storeId, "Store id cannot be null");
        Objects.requireNonNull(employeeId,"Employee id cannot be null");
        Objects.requireNonNull(updateEmployeeDTO);

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

    public void deleteEmployee(Long storeId, Long employeeId) {
        Objects.requireNonNull(storeId,"Store id cannot be null");
        Objects.requireNonNull(employeeId,"Employee id cannot be null");

        Employee employee = getEmployeeIfBelongsToStore(storeId, employeeId);

        employeeRepository.delete(employee);
    }

    public List<ResponseEmployeeDTO> findAll() {
        return employeeRepository.findAll().stream()
                .map(employeeMapper::toResponseEmployeeDTO)
                .toList();
    }

    public ResponseEmployeeDTO findById(Long id) {
        Objects.requireNonNull(id,"Id cannot be null");

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find employee by id " + id));

        return employeeMapper.toResponseEmployeeDTO(employee);
    }

    public boolean existsById(Long id) {
        Objects.requireNonNull(id,"Id cannot be null");

        return employeeRepository.existsById(id);
    }

    public boolean existsBySap(Long sap) {
        Objects.requireNonNull(sap,"Sap cannot be null");

        return employeeRepository.existsBySap(sap);
    }

    public boolean existsByLastName(String lastName) {
        Objects.requireNonNull(lastName,"Last name cannot be null");

        return employeeRepository.existsByLastName(lastName);
    }

    private Employee getEmployeeIfBelongsToStore(Long storeId, Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find employee by id " + employeeId));

        if (!employee.getStore().getId().equals(storeId)){
            throw new AccessDeniedException("Employee does not belong to this store");
        }

        return employee;
    }
}
