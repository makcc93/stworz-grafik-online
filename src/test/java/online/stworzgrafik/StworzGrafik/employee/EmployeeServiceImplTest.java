package online.stworzgrafik.StworzGrafik.employee;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.employee.TestCreateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.employee.TestEmployeeBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.employee.TestResponseEmployeeDTO;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.position.TestCreatePositionDTO;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.position.TestPositionBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.store.TestStoreBuilder;
import online.stworzgrafik.StworzGrafik.employee.DTO.CreateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.ResponseEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.position.Position;
import online.stworzgrafik.StworzGrafik.employee.position.PositionRepository;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreRepository;
import online.stworzgrafik.StworzGrafik.validator.NameValidatorService;
import online.stworzgrafik.StworzGrafik.validator.ObjectType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {
    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private EmployeeBuilder employeeBuilder;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private NameValidatorService nameValidatorService;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private PositionRepository positionRepository;

    @Test
    void createEmployee_workingTest(){
        //given
        String firstName = "FIRST NAME";
        String lastName = "LAST NAME";
        Long sap = 112233L;
        CreateEmployeeDTO createEmployeeDTO = new TestCreateEmployeeDTO()
                .withFirstName(firstName)
                .withLastName(lastName)
                .withSap(sap)
                .build();

        when(employeeRepository.existsBySap(createEmployeeDTO.sap())).thenReturn(false);
        when(employeeRepository.existsByLastName(createEmployeeDTO.lastName())).thenReturn(false);

        when(nameValidatorService.validate(createEmployeeDTO.firstName(), ObjectType.PERSON)).thenReturn(firstName);
        when(nameValidatorService.validate(createEmployeeDTO.lastName(), ObjectType.PERSON)).thenReturn(lastName);
        when((nameValidatorService.validate(createEmployeeDTO.sap().toString(), ObjectType.SAP))).thenReturn(sap.toString());

        Store store = new TestStoreBuilder().build();
        when(storeRepository.findById(createEmployeeDTO.storeId())).thenReturn(Optional.ofNullable(store));

        Position position = new TestPositionBuilder().build();
        when(positionRepository.findById(createEmployeeDTO.positionId())).thenReturn(Optional.ofNullable(position));

        Employee employee = new TestEmployeeBuilder()
                .withFirstName(createEmployeeDTO.firstName())
                .withLastName(createEmployeeDTO.lastName())
                .withSap(createEmployeeDTO.sap())
                .withStore(store)
                .withPosition(position)
                .build();

        when(employeeBuilder.createEmployee(
                createEmployeeDTO.firstName(),
                createEmployeeDTO.lastName(),
                createEmployeeDTO.sap(),
                store,
                position
        )).thenReturn(employee);

        when(employeeRepository.save(employee)).thenReturn(employee);

        ResponseEmployeeDTO responseEmployeeDTO = new TestResponseEmployeeDTO()
                .withFirstName(createEmployeeDTO.firstName())
                .withLastName(createEmployeeDTO.lastName())
                .withSap(createEmployeeDTO.sap())
                .withStoreId(createEmployeeDTO.storeId())
                .withPositionId(createEmployeeDTO.positionId())
                .build();

        when(employeeMapper.toResponseEmployeeDTO(employee)).thenReturn(responseEmployeeDTO);

        //when
        ResponseEmployeeDTO serviceResponse = employeeService.createEmployee(createEmployeeDTO);

        //then
        assertEquals(firstName,serviceResponse.firstName());
        assertEquals(lastName,serviceResponse.lastName());
        assertEquals(sap,serviceResponse.sap());
    }

    @Test
    void createEmployee_dtoIsNullThrowsException(){
        //given
        CreateEmployeeDTO createEmployeeDTO = null;

        //when
        assertThrows(NullPointerException.class, () -> employeeService.createEmployee(createEmployeeDTO));

        //then
    }

    @Test
    void createEmployee_employeeWithThisSapAlreadyExistsThrowsException(){
        //given
        Long sap = 100200300L;

        CreateEmployeeDTO createEmployeeDTO = new TestCreateEmployeeDTO().withSap(sap).build();

        when(employeeRepository.existsBySap(createEmployeeDTO.sap())).thenReturn(true);

        //when
        EntityExistsException exception =
                assertThrows(EntityExistsException.class, () -> employeeService.createEmployee(createEmployeeDTO));

        //then
        assertEquals("Employee with sap " + sap + " already exists", exception.getMessage());

        verify(employeeRepository,never()).existsByLastName(any());
        verify(nameValidatorService,never()).validate(any(),any());
        verify(storeRepository,never()).findById(any(Long.class));
        verify(positionRepository,never()).findById(any(Long.class));
        verify(employeeBuilder,never()).createEmployee(any(),any(),any(),any(),any());
        verify(employeeRepository,never()).save(any());
        verify(employeeMapper,never()).toResponseEmployeeDTO(any());

    }

    @Test
    void createEmployee_employeeWithThisLastNameAlreadyExistsThrowsException(){
        //given
        String lastName = "KNOWN LAST NAME";

        CreateEmployeeDTO createEmployeeDTO = new TestCreateEmployeeDTO().withLastName(lastName).build();

        when(employeeRepository.existsBySap(createEmployeeDTO.sap())).thenReturn(false);

        when(employeeRepository.existsByLastName(createEmployeeDTO.lastName())).thenReturn(true);

        //when
        EntityExistsException exception =
                assertThrows(EntityExistsException.class, () -> employeeService.createEmployee(createEmployeeDTO));

        //then
        assertEquals("Employee with last name " + lastName + " already exists", exception.getMessage());

        verify(nameValidatorService,never()).validate(any(),any());
        verify(storeRepository,never()).findById(any(Long.class));
        verify(positionRepository,never()).findById(any(Long.class));
        verify(employeeBuilder,never()).createEmployee(any(),any(),any(),any(),any());
        verify(employeeRepository,never()).save(any());
        verify(employeeMapper,never()).toResponseEmployeeDTO(any());
    }

    @Test
    void createEmployee_cannotFindStoreThrowsException(){
        //given
        String firstName = "FIRST NAME";
        String lastName = "LAST NAME";
        Long sap = 112233L;
        CreateEmployeeDTO createEmployeeDTO = new TestCreateEmployeeDTO().build();

        when(employeeRepository.existsBySap(createEmployeeDTO.sap())).thenReturn(false);
        when(employeeRepository.existsByLastName(createEmployeeDTO.lastName())).thenReturn(false);

        when(nameValidatorService.validate(createEmployeeDTO.firstName(), ObjectType.PERSON)).thenReturn(firstName);
        when(nameValidatorService.validate(createEmployeeDTO.lastName(), ObjectType.PERSON)).thenReturn(lastName);
        when((nameValidatorService.validate(createEmployeeDTO.sap().toString(), ObjectType.SAP))).thenReturn(sap.toString());

        when(storeRepository.findById(createEmployeeDTO.storeId())).thenReturn(Optional.empty());

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> employeeService.createEmployee(createEmployeeDTO));

        //then
        assertEquals("Cannot find store by id " + createEmployeeDTO.storeId(), exception.getMessage());
        verify(positionRepository,never()).findById(any(Long.class));
        verify(employeeBuilder,never()).createEmployee(any(),any(),any(),any(),any());
        verify(employeeRepository,never()).save(any());
        verify(employeeMapper,never()).toResponseEmployeeDTO(any());
    }

    @Test
    void createEmployee_cannotFindPositionThrowsException(){
        //given
        String firstName = "FIRST NAME";
        String lastName = "LAST NAME";
        Long sap = 112233L;
        CreateEmployeeDTO createEmployeeDTO = new TestCreateEmployeeDTO().build();

        when(employeeRepository.existsBySap(createEmployeeDTO.sap())).thenReturn(false);
        when(employeeRepository.existsByLastName(createEmployeeDTO.lastName())).thenReturn(false);

        when(nameValidatorService.validate(createEmployeeDTO.firstName(), ObjectType.PERSON)).thenReturn(firstName);
        when(nameValidatorService.validate(createEmployeeDTO.lastName(), ObjectType.PERSON)).thenReturn(lastName);
        when((nameValidatorService.validate(createEmployeeDTO.sap().toString(), ObjectType.SAP))).thenReturn(sap.toString());

        Store store = new TestStoreBuilder().build();
        when(storeRepository.findById(createEmployeeDTO.storeId())).thenReturn(Optional.ofNullable(store));

        when(positionRepository.findById(createEmployeeDTO.positionId())).thenReturn(Optional.empty());

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> employeeService.createEmployee(createEmployeeDTO));

        //then
        assertEquals("Cannot find position by id " + createEmployeeDTO.positionId(),exception.getMessage());
        verify(employeeBuilder,never()).createEmployee(any(),any(),any(),any(),any());
        verify(employeeRepository,never()).save(any());
        verify(employeeMapper,never()).toResponseEmployeeDTO(any());
    }


}