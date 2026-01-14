package online.stworzgrafik.StworzGrafik.employee;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PrePersist;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.employee.position.PositionEntityService;
import online.stworzgrafik.StworzGrafik.employee.position.PositionService;
import online.stworzgrafik.StworzGrafik.employee.position.TestPositionBuilder;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.*;
import online.stworzgrafik.StworzGrafik.employee.DTO.CreateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.ResponseEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.UpdateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.position.Position;
import online.stworzgrafik.StworzGrafik.validator.NameValidatorService;
import online.stworzgrafik.StworzGrafik.validator.ObjectType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {
    @InjectMocks
    private EmployeeServiceImpl employeeServiceImpl;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private EmployeeBuilder employeeBuilder;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private NameValidatorService nameValidatorService;

    @Mock
    private StoreService storeService;

    @Mock
    private StoreEntityService storeEntityService;

    @Mock
    private PositionService positionService;

    @Mock
    private PositionEntityService positionEntityService;

    @Mock
    private UserAuthorizationService userAuthorizationService;

    private Region region;
    private Branch branch;
    private Store store;
    private Long storeId;

    @PrePersist
    void setup(){
        region = new TestRegionBuilder().build();
        branch = new TestBranchBuilder().withRegion(region).build();
        store = new TestStoreBuilder().withBranch(branch).build();

        storeId = store.getId();
    }

    @Test
    void createEmployee_workingTest(){
        //given
        String firstName = "FIRST NAME";
        String lastName = "LAST NAME";
        Long storeId = 1L;
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
        when(storeService.existsById(storeId)).thenReturn(true);
        when(storeEntityService.getEntityById(storeId)).thenReturn(store);

        Position position = new TestPositionBuilder().build();
        when(positionService.exists(createEmployeeDTO.positionId())).thenReturn(true);
        when(positionEntityService.getEntityById(createEmployeeDTO.positionId())).thenReturn(position);

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
                .withStoreId(storeId)
                .withPositionId(createEmployeeDTO.positionId())
                .build();

        when(employeeMapper.toResponseEmployeeDTO(employee)).thenReturn(responseEmployeeDTO);

        //when
        ResponseEmployeeDTO serviceResponse = employeeServiceImpl.createEmployee(storeId,createEmployeeDTO);

        //then
        assertEquals(firstName,serviceResponse.firstName());
        assertEquals(lastName,serviceResponse.lastName());
        assertEquals(sap,serviceResponse.sap());
    }

    @Test
    void createEmployee_dtoIsNullThrowsException(){
        //given
        Long storeId = 1L;
        CreateEmployeeDTO createEmployeeDTO = null;

        //when
        assertThrows(NullPointerException.class, () -> employeeServiceImpl.createEmployee(storeId,createEmployeeDTO));

        //then
        verify(storeService,never()).findById(any(Long.class));
        verify(positionService,never()).findById(any(Long.class));
        verify(employeeBuilder,never()).createEmployee(any(),any(),any(),any(),any());
        verify(employeeRepository,never()).save(any());
        verify(employeeMapper,never()).toResponseEmployeeDTO(any());
    }

    @Test
    void createEmployee_employeeWithThisSapAlreadyExistsThrowsException(){
        //given
        Long sap = 100200300L;
        Long storeId = 1L;

        CreateEmployeeDTO createEmployeeDTO = new TestCreateEmployeeDTO().withSap(sap).build();

        when(employeeRepository.existsBySap(createEmployeeDTO.sap())).thenReturn(true);

        //when
        EntityExistsException exception =
                assertThrows(EntityExistsException.class, () -> employeeServiceImpl.createEmployee(storeId,createEmployeeDTO));

        //then
        assertEquals("Employee with sap " + sap + " already exists", exception.getMessage());

        verify(nameValidatorService,never()).validate(any(),any());
        verify(storeService,never()).findById(any(Long.class));
        verify(positionService,never()).findById(any(Long.class));
        verify(employeeBuilder,never()).createEmployee(any(),any(),any(),any(),any());
        verify(employeeRepository,never()).save(any());
        verify(employeeMapper,never()).toResponseEmployeeDTO(any());

    }

    @Test
    void createEmployee_cannotFindStoreThrowsException(){
        //given
        String firstName = "FIRST NAME";
        String lastName = "LAST NAME";
        Long storeId = 1L;
        Long sap = 11223344L;
        CreateEmployeeDTO createEmployeeDTO = new TestCreateEmployeeDTO().build();

        when(employeeRepository.existsBySap(createEmployeeDTO.sap())).thenReturn(false);

        when(nameValidatorService.validate(createEmployeeDTO.firstName(), ObjectType.PERSON)).thenReturn(firstName);
        when(nameValidatorService.validate(createEmployeeDTO.lastName(), ObjectType.PERSON)).thenReturn(lastName);

        when(storeService.existsById(storeId)).thenReturn(false);

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> employeeServiceImpl.createEmployee(storeId,createEmployeeDTO));

        //then
        assertEquals("Cannot find store by id " + storeId, exception.getMessage());
        verify(positionService,never()).findById(any(Long.class));
        verify(employeeBuilder,never()).createEmployee(any(),any(),any(),any(),any());
        verify(employeeRepository,never()).save(any());
        verify(employeeMapper,never()).toResponseEmployeeDTO(any());
    }

    @Test
    void createEmployee_cannotFindPositionThrowsException(){
        //given
        String firstName = "FIRST NAME";
        String lastName = "LAST NAME";
        Long storeId = 1L;
        Long sap = 11223344L;
        CreateEmployeeDTO createEmployeeDTO = new TestCreateEmployeeDTO().build();

        when(employeeRepository.existsBySap(createEmployeeDTO.sap())).thenReturn(false);

        when(nameValidatorService.validate(createEmployeeDTO.firstName(), ObjectType.PERSON)).thenReturn(firstName);
        when(nameValidatorService.validate(createEmployeeDTO.lastName(), ObjectType.PERSON)).thenReturn(lastName);

        when(storeService.existsById(storeId)).thenReturn(true);
        when(storeEntityService.getEntityById(storeId)).thenReturn(store);

        when(positionService.exists(createEmployeeDTO.positionId())).thenReturn(false);

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> employeeServiceImpl.createEmployee(storeId,createEmployeeDTO));

        //then
        assertEquals("Cannot find position by id " + createEmployeeDTO.positionId(),exception.getMessage());
        verify(employeeBuilder,never()).createEmployee(any(),any(),any(),any(),any());
        verify(employeeRepository,never()).save(any());
        verify(employeeMapper,never()).toResponseEmployeeDTO(any());
    }

    @Test
    void updateEmployee_workingTest(){
        //given
        Long storeId = 1L;
        Long employeeId = 1L;
        Store store = mock(Store.class);
        when(store.getId()).thenReturn(storeId);

        String originalFirstName = "ORIGINAL FIRST NAME";
        String originalLastName = "ORIGINAL LAST NAME";
        Employee employee = new TestEmployeeBuilder().withStore(store).withFirstName(originalFirstName).withLastName(originalLastName).build();
        when(employeeRepository.save(employee)).thenReturn(employee);

        String newFirstName = "NEW FIRST NAME";
        String newLastName = "NEW LAST NAME";
        UpdateEmployeeDTO updateEmployeeDTO = new TestUpdateEmployeeDTO().withFirstName(newFirstName).withLastName(newLastName).build();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.ofNullable(employee));
        when(nameValidatorService.validate(updateEmployeeDTO.firstName(),ObjectType.PERSON)).thenReturn(newFirstName);
        when(nameValidatorService.validate(updateEmployeeDTO.lastName(),ObjectType.PERSON)).thenReturn(newLastName);

        ResponseEmployeeDTO responseEmployeeDTO = new TestResponseEmployeeDTO().withFirstName(newFirstName).withLastName(newLastName).build();
        when(employeeMapper.toResponseEmployeeDTO(employee)).thenReturn(responseEmployeeDTO);

        //when
        ResponseEmployeeDTO serviceResponse = employeeServiceImpl.updateEmployee(storeId,employeeId, updateEmployeeDTO);

        //then
        assertEquals(newFirstName,serviceResponse.firstName());
        assertEquals(newLastName,serviceResponse.lastName());
        assertEquals(employee.getSap(),serviceResponse.sap());
    }

    @Test
    void updateEmployee_cannotFindEntityByIdThrowsException(){
        //given
        Long storeId = 1L;
        Long employeeId = 1234L;
        UpdateEmployeeDTO updateEmployeeDTO = new TestUpdateEmployeeDTO().build();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> employeeServiceImpl.updateEmployee(storeId,employeeId, updateEmployeeDTO));

        //then
        assertEquals("Cannot find employee by id " + employeeId, exception.getMessage());

        verify(nameValidatorService,never()).validate(any(),any());
        verify(employeeMapper,never()).updateEmployee(any(),any());
        verify(employeeRepository,never()).save(any());
        verify(employeeMapper,never()).toResponseEmployeeDTO(any());
    }

    @Test
    void deleteEmployee_workingTest(){
        //given
        Long employeeId = 123L;
        Long storeId = 1L;
        Store store = mock(Store.class);
        when(store.getId()).thenReturn(storeId);

        Employee employee = new TestEmployeeBuilder().withStore(store).build();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.ofNullable(employee));


        //when
        employeeServiceImpl.deleteEmployee(storeId,employeeId);

        //then
        verify(employeeRepository,times(1)).delete(employee);
        verify(employeeRepository,times(1)).findById(employeeId);
    }

    @Test
    void deleteEmployee_employeeByIdDoesNotExistThrowsException(){
        //given
        Long employeeId = 1L;
        Long storeId = 1L;

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> employeeServiceImpl.deleteEmployee(storeId,employeeId));

        //then
        assertEquals("Cannot find employee by id " + employeeId, exception.getMessage());

        verify(employeeRepository,times(1)).findById(employeeId);
        verify(employeeRepository,never()).delete(any(Employee.class));
    }

    @Test
    void deleteEmployee_employeeDoesNotBelongToStoreThrowsException(){
        //given
        Long employeeId = 12345L;
        Long storeId = 1L;
        Store store = mock(Store.class);

        Employee employee = new TestEmployeeBuilder().withStore(store).build();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.ofNullable(employee));

        when(store.getId()).thenReturn(2L);

        //when
        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> employeeServiceImpl.deleteEmployee(storeId, employeeId));

        //then
        assertEquals("Employee does not belong to this store", exception.getMessage());

        verify(employeeRepository,never()).delete(any(Employee.class));
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
        List<ResponseEmployeeDTO> serviceResponse = employeeServiceImpl.findAll();

        //then
        assertTrue(serviceResponse.containsAll(employeesDTOs));
    }

    @Test
    void findAll_emptyListDoesNotThrowException(){
        //given

        //when
        List<ResponseEmployeeDTO> serviceResponse = employeeServiceImpl.findAll();

        //then
        assertEquals(0, serviceResponse.size());
        assertDoesNotThrow(() -> employeeServiceImpl.findAll());
    }

    @Test
    void findById_workingTest(){
        //given
        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        Long id = 6988L;
        Employee employee = new TestEmployeeBuilder().build();

        when(employeeRepository.findById(id)).thenReturn(Optional.ofNullable(employee));

        ResponseEmployeeDTO responseEmployeeDTO = new TestResponseEmployeeDTO().fromEmployee(employee).build();
        when(employeeMapper.toResponseEmployeeDTO(employee)).thenReturn(responseEmployeeDTO);

        //when
        ResponseEmployeeDTO serviceResponse = employeeServiceImpl.findById(storeId, id);

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
        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        Long randomId = 1234L;

        when(employeeRepository.findById(randomId)).thenReturn(Optional.empty());

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> employeeServiceImpl.findById(storeId,randomId));

        //then
        assertEquals("Cannot find employee by id " + randomId, exception.getMessage());
        verify(employeeMapper,never()).toResponseEmployeeDTO(any());
    }

    @Test
    void existsById_workingTest(){
        //given
        Long id = 321L;
        Employee employee = new TestEmployeeBuilder().build();

        when(employeeRepository.existsById(id)).thenReturn(true);

        //when
        boolean serviceResponse = employeeServiceImpl.existsById(id);

        //then
        assertTrue(serviceResponse);
        verify(employeeRepository,times(1)).existsById(id);
    }

    @Test
    void existsBySap_workingTest(){
        //given
        Long sap = 87654321L;
        Employee employee = new TestEmployeeBuilder().withSap(sap).build();

        when(employeeRepository.existsBySap(sap)).thenReturn(true);

        //when
        boolean serviceResponse = employeeServiceImpl.existsBySap(sap);

        //then
        assertTrue(serviceResponse);
    }

    @Test
    void existsByLastName_workingTest(){
        //given
        String lastName = "LAST-NAME";
        Employee employee = new TestEmployeeBuilder().withLastName(lastName).build();
        
        when(employeeRepository.existsByLastName(lastName)).thenReturn(true);
        
        //when
        boolean serviceResponse = employeeServiceImpl.existsByLastName(lastName);
        
        //then
        assertTrue(serviceResponse);
    }
}