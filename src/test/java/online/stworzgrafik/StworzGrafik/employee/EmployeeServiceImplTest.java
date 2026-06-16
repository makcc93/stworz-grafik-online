package online.stworzgrafik.StworzGrafik.employee;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.employee.position.PositionEntityService;
import online.stworzgrafik.StworzGrafik.employee.position.PositionService;
import online.stworzgrafik.StworzGrafik.employee.position.TestPositionBuilder;
import online.stworzgrafik.StworzGrafik.employee.workNorm.SpecialWorkNormEntityService;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Mock
    private SpecialWorkNormEntityService specialWorkNormEntityService;

    private Region region;
    private Branch branch;
    private Store store;
    private Long storeId;
    private Pageable pageable;

    @BeforeEach
    void setup(){
        region = new TestRegionBuilder().build();
        branch = new TestBranchBuilder().withRegion(region).build();
        store = new TestStoreBuilder().withBranch(branch).build();

        storeId = store.getId();
        pageable = PageRequest.of(0,20);
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
                .buildDefault();

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

        //when
        assertThrows(NullPointerException.class, () -> employeeServiceImpl.createEmployee(storeId,null));

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

        Store localStore = new TestStoreBuilder().build();
        when(storeEntityService.getEntityById(storeId)).thenReturn(localStore);

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
        Employee employee = new TestEmployeeBuilder().withStore(store).withFirstName(originalFirstName).withLastName(originalLastName).buildDefault();
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
        verify(employeeMapper, times(1)).updateEmployee(any(UpdateEmployeeDTO.class), eq(employee));
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

        Employee employee = new TestEmployeeBuilder().withStore(store).buildDefault();
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

        Employee employee = new TestEmployeeBuilder().withStore(store).buildDefault();
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
    void findAll_workingTest() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        Employee e1 = new TestEmployeeBuilder().withFirstName("FIRST").build();  // build() zamiast buildDefault()
        Employee e2 = new TestEmployeeBuilder().withFirstName("SECOND").build();
        Employee e3 = new TestEmployeeBuilder().withFirstName("THIRD").build();
        List<Employee> employees = List.of(e1, e2, e3);
        Page<Employee> employeePage = new PageImpl<>(employees, pageable, employees.size());

        when(employeeRepository.findAll(pageable)).thenReturn(employeePage);

        ResponseEmployeeDTO dto1 = new TestResponseEmployeeDTO().fromEmployee(e1).build();
        ResponseEmployeeDTO dto2 = new TestResponseEmployeeDTO().fromEmployee(e2).build();
        ResponseEmployeeDTO dto3 = new TestResponseEmployeeDTO().fromEmployee(e3).build();
        when(employeeMapper.toResponseEmployeeDTO(e1)).thenReturn(dto1);
        when(employeeMapper.toResponseEmployeeDTO(e2)).thenReturn(dto2);
        when(employeeMapper.toResponseEmployeeDTO(e3)).thenReturn(dto3);

        // when
        Page<ResponseEmployeeDTO> result = employeeServiceImpl.findAll(pageable);

        // then
        verify(employeeRepository).findAll(pageable);
        verify(employeeMapper, times(3)).toResponseEmployeeDTO(any(Employee.class));

        assertEquals(3, result.getTotalElements());
        assertEquals(3, result.getContent().size());
        assertEquals("FIRST", result.getContent().get(0).firstName());
        assertEquals("SECOND", result.getContent().get(1).firstName());
        assertEquals("THIRD", result.getContent().get(2).firstName());
    }

    @Test
    void findById_workingTest(){
        //given
        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        Long id = 6988L;
        Employee employee = new TestEmployeeBuilder().buildDefault();

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

        when(employeeRepository.existsByLastName(lastName)).thenReturn(true);

        //when
        boolean serviceResponse = employeeServiceImpl.existsByLastName(lastName);

        //then
        assertTrue(serviceResponse);
    }
}