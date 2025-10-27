package online.stworzgrafik.StworzGrafik.employee;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.employee.TestCreateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.employee.TestEmployeeBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.employee.TestResponseEmployeeDTO;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.employee.TestUpdateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.position.TestPositionBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.store.TestStoreBuilder;
import online.stworzgrafik.StworzGrafik.employee.DTO.CreateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.ResponseEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.UpdateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.position.Position;
import online.stworzgrafik.StworzGrafik.employee.position.PositionRepository;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreRepository;
import online.stworzgrafik.StworzGrafik.validator.NameValidatorService;
import online.stworzgrafik.StworzGrafik.validator.ObjectType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
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

        when(nameValidatorService.validate(createEmployeeDTO.firstName(), ObjectType.PERSON)).thenReturn(firstName);
        when(nameValidatorService.validate(createEmployeeDTO.lastName(), ObjectType.PERSON)).thenReturn(lastName);

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

        when(nameValidatorService.validate(createEmployeeDTO.firstName(), ObjectType.PERSON)).thenReturn(firstName);
        when(nameValidatorService.validate(createEmployeeDTO.lastName(), ObjectType.PERSON)).thenReturn(lastName);

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

        when(nameValidatorService.validate(createEmployeeDTO.firstName(), ObjectType.PERSON)).thenReturn(firstName);
        when(nameValidatorService.validate(createEmployeeDTO.lastName(), ObjectType.PERSON)).thenReturn(lastName);

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

    @Test
    void updateEmployee_workingTest(){
        //given
        Long id = 1L;

        String originalFirstName = "ORIGINAL FIRST NAME";
        String originalLastName = "ORIGINAL LAST NAME";
        Employee employee = new TestEmployeeBuilder().withFirstName(originalFirstName).withLastName(originalLastName).build();

        String newFirstName = "NEW FIRST NAME";
        String newLastName = "NEW LAST NAME";
        UpdateEmployeeDTO updateEmployeeDTO = new TestUpdateEmployeeDTO().withFirstName(newFirstName).withLastName(newLastName).build();

        when(employeeRepository.findById(id)).thenReturn(Optional.ofNullable(employee));

        when(nameValidatorService.validate(updateEmployeeDTO.firstName(),ObjectType.PERSON)).thenReturn(newFirstName);
        when(nameValidatorService.validate(updateEmployeeDTO.lastName(),ObjectType.PERSON)).thenReturn(newLastName);

        when(employeeRepository.save(employee)).thenReturn(employee);

        ResponseEmployeeDTO responseEmployeeDTO = new TestResponseEmployeeDTO().withFirstName(newFirstName).withLastName(newLastName).build();
        when(employeeMapper.toResponseEmployeeDTO(employee)).thenReturn(responseEmployeeDTO);

        //when
        ResponseEmployeeDTO serviceResponse = employeeService.updateEmployee(id, updateEmployeeDTO);

        //then
        assertEquals(newFirstName,serviceResponse.firstName());
        assertEquals(newLastName,serviceResponse.lastName());
        assertEquals(employee.getSap(),serviceResponse.sap());
    }

    @Test
    void updateEmployee_idIsNullThrowsException(){
        //given
        Long id = null;
        UpdateEmployeeDTO updateEmployeeDTO = new TestUpdateEmployeeDTO().build();

        //when
        NullPointerException exception =
                assertThrows(NullPointerException.class, () -> employeeService.updateEmployee(id, updateEmployeeDTO));

        //then
        assertEquals("Id cannot be null", exception.getMessage());

        verify(employeeRepository,never()).findById(id);
        verify(nameValidatorService,never()).validate(any(),any());
        verify(employeeMapper,never()).updateEmployee(any(),any());
        verify(employeeRepository,never()).save(any());
        verify(employeeMapper,never()).toResponseEmployeeDTO(any());
    }

    @Test
    void updateEmployee_dtoIsNullThrowsException(){
        //given
        Long id = 1L;
        UpdateEmployeeDTO updateEmployeeDTO = null;

        //when
        assertThrows(NullPointerException.class, () -> employeeService.updateEmployee(id, updateEmployeeDTO));

        //then
        verify(employeeRepository,never()).findById(id);
        verify(nameValidatorService,never()).validate(any(),any());
        verify(employeeMapper,never()).updateEmployee(any(),any());
        verify(employeeRepository,never()).save(any());
        verify(employeeMapper,never()).toResponseEmployeeDTO(any());
    }

    @Test
    void updateEmployee_cannotFindEntityByIdThrowsException(){
        //given
        Long id = 1234L;
        UpdateEmployeeDTO updateEmployeeDTO = new TestUpdateEmployeeDTO().build();

        when(employeeRepository.findById(id)).thenReturn(Optional.empty());

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> employeeService.updateEmployee(id, updateEmployeeDTO));

        //then
        assertEquals("Cannot find employee by id " + id, exception.getMessage());

        verify(nameValidatorService,never()).validate(any(),any());
        verify(employeeMapper,never()).updateEmployee(any(),any());
        verify(employeeRepository,never()).save(any());
        verify(employeeMapper,never()).toResponseEmployeeDTO(any());
    }

    @Test
    void deleteEmployee_workingTest(){
        //given
        Long id = 123L;

        when(employeeRepository.existsById(id)).thenReturn(true);

        //when
        employeeService.deleteEmployee(id);

        //then
        verify(employeeRepository,times(1)).deleteById(id);
        verify(employeeRepository,times(1)).existsById(id);
    }

    @Test
    void deleteEmployee_employeeByIdDoesNotExistThrowsException(){
        //given
        Long id = 1L;

        when(employeeRepository.existsById(id)).thenReturn(false);

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> employeeService.deleteEmployee(id));

        //then
        assertEquals("Cannot find employee by id " + id, exception.getMessage());

        verify(employeeRepository,times(1)).existsById(id);
        verify(employeeRepository,never()).deleteById(id);
    }

    @Test
    void deleteEmployee_idIsNullThrowsException(){
        //given
        Long id = null;

        //when
        NullPointerException exception =
                assertThrows(NullPointerException.class, () -> employeeService.deleteEmployee(id));

        //then
        assertEquals("Id cannot be null", exception.getMessage());

        verify(employeeRepository,never()).existsById(any());
        verify(employeeRepository,never()).deleteById(any());
    }

    @Test
    void findAll_workingTest(){
        //given
        String first = "FIRST";
        String second = "SECOND";
        String third = "THIRD";

        Employee firstEmployee = new TestEmployeeBuilder().withFirstName(first).build();
        Employee secondEmployee = new TestEmployeeBuilder().withFirstName(second).build();
        Employee thirdEmployee = new TestEmployeeBuilder().withFirstName(third).build();
        List<Employee> employees = List.of(firstEmployee,secondEmployee,thirdEmployee);

        when(employeeRepository.findAll()).thenReturn(employees);

        ResponseEmployeeDTO firstResponseEmployeeDTO = new TestResponseEmployeeDTO().fromEmployee(firstEmployee).build();
        ResponseEmployeeDTO secondResponseEmployeeDTO = new TestResponseEmployeeDTO().fromEmployee(secondEmployee).build();
        ResponseEmployeeDTO thirdResponseEmployeeDTO = new TestResponseEmployeeDTO().fromEmployee(thirdEmployee).build();

        when(employeeMapper.toResponseEmployeeDTO(firstEmployee)).thenReturn(firstResponseEmployeeDTO);
        when(employeeMapper.toResponseEmployeeDTO(secondEmployee)).thenReturn(secondResponseEmployeeDTO);
        when(employeeMapper.toResponseEmployeeDTO(thirdEmployee)).thenReturn(thirdResponseEmployeeDTO);

        List<ResponseEmployeeDTO> employeesDTOs = List.of(firstResponseEmployeeDTO,secondResponseEmployeeDTO,thirdResponseEmployeeDTO);

        //when
        List<ResponseEmployeeDTO> serviceResponse = employeeService.findAll();

        //then
        assertTrue(serviceResponse.containsAll(employeesDTOs));
    }

    @Test
    void findAll_emptyListDoesNotThrowException(){
        //given

        //when
        List<ResponseEmployeeDTO> serviceResponse = employeeService.findAll();

        //then
        assertEquals(0, serviceResponse.size());
        assertDoesNotThrow(() -> employeeService.findAll());
    }

    @Test
    void findById_workingTest(){
        //given
        Long id = 6988L;
        Employee employee = new TestEmployeeBuilder().build();

        when(employeeRepository.findById(id)).thenReturn(Optional.ofNullable(employee));

        ResponseEmployeeDTO responseEmployeeDTO = new TestResponseEmployeeDTO().fromEmployee(employee).build();
        when(employeeMapper.toResponseEmployeeDTO(employee)).thenReturn(responseEmployeeDTO);

        //when
        ResponseEmployeeDTO serviceResponse = employeeService.findById(id);

        //then
        assertEquals(responseEmployeeDTO.firstName(), serviceResponse.firstName());
        assertEquals(responseEmployeeDTO.lastName(),serviceResponse.lastName());
        assertEquals(responseEmployeeDTO.sap(),serviceResponse.sap());
        assertEquals(responseEmployeeDTO.storeId(),serviceResponse.storeId());
        assertEquals(responseEmployeeDTO.positionId(),serviceResponse.positionId());
        assertEquals(responseEmployeeDTO.canOperateCheckout(),serviceResponse.canOperateCheckout());
        assertEquals(responseEmployeeDTO.canOperateCredit(),serviceResponse.canOperateCredit());
        assertEquals(responseEmployeeDTO.canOpenCloseStore(),serviceResponse.canOpenCloseStore());
        assertEquals(responseEmployeeDTO.seller(),serviceResponse.seller());
        assertEquals(responseEmployeeDTO.manager(),serviceResponse.manager());
        assertEquals(responseEmployeeDTO.createdAt(),serviceResponse.createdAt());
        assertEquals(responseEmployeeDTO.updatedAt(),serviceResponse.updatedAt());
    }

    @Test
    void findById_cannotFindEmployeeThrowsException(){
        //given
        Long randomId = 1234L;

        when(employeeRepository.findById(randomId)).thenReturn(Optional.empty());

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> employeeService.findById(randomId));

        //then
        assertEquals("Cannot find employee by id " + randomId, exception.getMessage());
        verify(employeeMapper,never()).toResponseEmployeeDTO(any());
    }

    @Test
    void findById_idIsNullThrowsException(){
        //given
        Long id = null;

        //when
        NullPointerException exception =
                assertThrows(NullPointerException.class, () -> employeeService.findById(id));

        //then
        assertEquals("Id cannot be null", exception.getMessage());

        verify(employeeRepository,never()).findById(any());
        verify(employeeMapper,never()).toResponseEmployeeDTO(any());
    }
}