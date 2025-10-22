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
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;

@Service
@Validated
public class EmployeeServiceImpl implements EmployeeService{
    private final EmployeeRepository employeeRepository;
    private final EmployeeBuilder employeeBuilder;
    private final NameValidatorService nameValidatorService;
    private final EmployeeMapper employeeMapper;
    private final StoreRepository storeRepository;
    private final PositionRepository positionRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, EmployeeBuilder employeeBuilder, NameValidatorService nameValidatorService, EmployeeMapper employeeMapper, StoreRepository storeRepository, PositionRepository positionRepository) {
        this.employeeRepository = employeeRepository;
        this.employeeBuilder = employeeBuilder;
        this.nameValidatorService = nameValidatorService;
        this.employeeMapper = employeeMapper;
        this.storeRepository = storeRepository;
        this.positionRepository = positionRepository;
    }

    @Override
    public ResponseEmployeeDTO createEmployee(CreateEmployeeDTO createEmployeeDTO) {
        Objects.requireNonNull(createEmployeeDTO);

        if (employeeRepository.existsBySap(createEmployeeDTO.sap())){
            throw new EntityExistsException("Employee with sap " + createEmployeeDTO.sap() + " already exists");
        }

        String validatedFirstName = nameValidatorService.validate(createEmployeeDTO.firstName(), ObjectType.PERSON);
        String validatedLastName = nameValidatorService.validate(createEmployeeDTO.lastName(), ObjectType.PERSON);

        Store store = storeRepository.findById(createEmployeeDTO.storeId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find store by id " + createEmployeeDTO.storeId()));
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

    @Override
    public ResponseEmployeeDTO update(UpdateEmployeeDTO updateEmployeeDTO) {
        return null;
    }

    @Override
    public void deleteEmployee(Long id) {

    }

    @Override
    public List<ResponseEmployeeDTO> findAll() {
        return List.of();
    }

    @Override
    public ResponseEmployeeDTO findById(Long id) {
        return null;
    }

    @Override
    public boolean existsById(Long id) {
        return false;
    }

    @Override
    public boolean existsBySap(Long sap) {
        return false;
    }

    @Override
    public boolean existsByLastName(String lastName) {
        return false;
    }

}
