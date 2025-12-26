package online.stworzgrafik.StworzGrafik.employee.proposal.daysOff;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PrePersist;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.employee.TestEmployeeBuilder;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.CreateEmployeeProposalDaysOffDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.ResponseEmployeeProposalDaysOffDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.UpdateEmployeeProposalDaysOffDTO;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeProposalDaysOffServiceImplTest {
    @InjectMocks
    private EmployeeProposalDaysOffServiceImpl service;

    @Mock
    private EmployeeProposalDaysOffBuilder builder;

    @Mock
    private EmployeeProposalDaysOffMapper mapper;

    @Mock
    private EmployeeProposalDaysOffRepository repository;

    @Mock
    private UserAuthorizationService userAuthorizationService;

    @Mock
    private StoreEntityService storeService;

    @Mock
    private EmployeeEntityService employeeService;

    private Long storeId = 1L;
    private Long employeeId = 9L;
    private Store store;
    private Employee employee;

    @PrePersist
    void setup(){
        store = new TestStoreBuilder().build();
        employee = new TestEmployeeBuilder().withStore(store).build();
    }

    @Test
    void createEmployeeProposalDaysOff_workingTest(){
        //given
        Store store = new TestStoreBuilder().build();
        Employee employee = new TestEmployeeBuilder().withStore(store).build();
        Integer year = 2025;
        Integer month = 12;
        int[] monthlyDaysOff = {1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1};

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(storeService.getEntityById(storeId)).thenReturn(store);

        when(employeeService.getEntityById(employeeId)).thenReturn(employee);

        when(repository.existsByStoreIdAndEmployeeIdAndYearAndMonth(storeId,employeeId,year,month)).thenReturn(false);

        EmployeeProposalDaysOff employeeProposalDaysOff = new TestEmployeeProposalDaysOffBuilder()
                .withStore(store)
                .withEmployee(employee)
                .withYear(year)
                .withMonth(month)
                .withMontlyDaysOff(monthlyDaysOff).build();

        when(builder.createEmployeeProposalDaysOff(store,employee,year,month,monthlyDaysOff)).thenReturn(employeeProposalDaysOff);

        ResponseEmployeeProposalDaysOffDTO responseEmployeeProposalDaysOffDTO = new TestResponseEmployeeProposalDaysOffDTO()
                .withStoreId(storeId)
                .withEmployeeId(employeeId)
                .withMonthlyDaysOff(monthlyDaysOff)
                .withYear(year)
                .withMonth(month).build();

        when(mapper.toResponseEmployeeProposalDaysOffDTO(employeeProposalDaysOff)).thenReturn(responseEmployeeProposalDaysOffDTO);

        CreateEmployeeProposalDaysOffDTO dto =
                new TestCreateEmployeeProposalDaysOffDTO()
                        .withYear(year)
                        .withMonth(month)
                        .withMonthlyDaysOff(monthlyDaysOff)
                        .build();
        //when
        ResponseEmployeeProposalDaysOffDTO serviceResponse = service.createEmployeeProposalDaysOff(storeId, employeeId, dto);

        //then
        assertEquals(storeId,serviceResponse.storeId());
        assertEquals(employeeId,serviceResponse.employeeId());
        assertEquals(year,serviceResponse.year());
        assertEquals(month,serviceResponse.month());
        assertEquals(monthlyDaysOff,serviceResponse.monthlyDaysOff());
    }

    @Test
    void createEmployeeProposalDaysOff_loggedUserHasNotAccessToStoreThrowsException(){
        //given
        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(false);

        CreateEmployeeProposalDaysOffDTO dto = new TestCreateEmployeeProposalDaysOffDTO().build();
        //when
        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> service.createEmployeeProposalDaysOff(storeId, employeeId, dto));

        //then
        assertEquals("Access denied for store with id " + storeId,exception.getMessage());

        verify(storeService,never()).getEntityById(any());
        verify(employeeService,never()).getEntityById(any());
        verify(repository,never()).existsByStoreIdAndEmployeeIdAndYearAndMonth(any(),any(),any(),any());
        verify(builder,never()).createEmployeeProposalDaysOff(any(),any(),any(),any(),any());
        verify(mapper,never()).toResponseEmployeeProposalDaysOffDTO(any());
    }

    @Test
    void createEmployeeProposalDaysOff_storeDoesNotExistThrowsException(){
        //given
        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(storeService.getEntityById(storeId)).thenThrow(EntityNotFoundException.class);

        CreateEmployeeProposalDaysOffDTO dto = new TestCreateEmployeeProposalDaysOffDTO().build();

        //when
        assertThrows(EntityNotFoundException.class, () -> service.createEmployeeProposalDaysOff(storeId, employeeId, dto));

        //then
        verify(userAuthorizationService,times(1)).hasAccessToStore(storeId);
        verify(employeeService,never()).getEntityById(storeId);
        verify(repository,never()).existsByStoreIdAndEmployeeIdAndYearAndMonth(any(),any(),any(),any());
        verify(builder,never()).createEmployeeProposalDaysOff(any(),any(),any(),any(),any());
        verify(mapper,never()).toResponseEmployeeProposalDaysOffDTO(any());
    }

    @Test
    void createEmployeeProposalDaysOff_employeeDoesNotExistThrowsException(){
        //given
        Store store = new TestStoreBuilder().build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(storeService.getEntityById(any())).thenReturn(store);

        when(employeeService.getEntityById(any())).thenThrow(EntityNotFoundException.class);

        CreateEmployeeProposalDaysOffDTO dto = new TestCreateEmployeeProposalDaysOffDTO().build();

        //when
        assertThrows(EntityNotFoundException.class, () -> service.createEmployeeProposalDaysOff(storeId, employeeId, dto));

        //then
        verify(userAuthorizationService,times(1)).hasAccessToStore(storeId);
        verify(storeService,times(1)).getEntityById(storeId);
        verify(repository,never()).existsByStoreIdAndEmployeeIdAndYearAndMonth(any(),any(),any(),any());
        verify(builder,never()).createEmployeeProposalDaysOff(any(),any(),any(),any(),any());
        verify(mapper,never()).toResponseEmployeeProposalDaysOffDTO(any());
    }

    @Test
    void createEmployeeProposalDaysOff_proposalForThisDayAlreadyExistsThrowsException(){
        //given
        Store store = new TestStoreBuilder().build();
        Employee employee = new TestEmployeeBuilder().withStore(store).build();
        Integer year = 2025;
        Integer month = 12;

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(storeService.getEntityById(storeId)).thenReturn(store);

        when(employeeService.getEntityById(employeeId)).thenReturn(employee);

        when(repository.existsByStoreIdAndEmployeeIdAndYearAndMonth(storeId,employeeId,year,month)).thenReturn(true);

        CreateEmployeeProposalDaysOffDTO dto = new TestCreateEmployeeProposalDaysOffDTO()
                .withYear(year)
                .withMonth(month)
                .build();

        //when
        EntityExistsException exception =
                assertThrows(EntityExistsException.class, () -> service.createEmployeeProposalDaysOff(storeId, employeeId, dto));

        //then
        assertEquals("Employee proposal days off in month " + dto.month() + " of  year " + dto.year() + " already exists",exception.getMessage());

        verify(userAuthorizationService,times(1)).hasAccessToStore(any());
        verify(storeService,times(1)).getEntityById(any());
        verify(employeeService,times(1)).getEntityById(any());
        verify(builder,never()).createEmployeeProposalDaysOff(any(),any(),any(),any(),any());
        verify(mapper,never()).toResponseEmployeeProposalDaysOffDTO(any());
    }

    @Test
    void updateEmployeeProposalDaysOff_workingTest(){
        //given
        Store store = new TestStoreBuilder().build();
        Employee employee = new TestEmployeeBuilder().withStore(store).build();
        Integer year = 2025;
        Integer month = 12;
        int[] monthlyDaysOff = {1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1};

        EmployeeProposalDaysOff employeeProposalDaysOff = new TestEmployeeProposalDaysOffBuilder()
                .withStore(store)
                .withEmployee(employee)
                .withYear(year)
                .withMonth(month)
                .withMontlyDaysOff(monthlyDaysOff).build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(repository.findByStoreIdAndEmployeeIdAndYearAndMonth(storeId,employeeId,year,month))
                .thenReturn(Optional.of(employeeProposalDaysOff));

        when(storeService.getEntityById(storeId)).thenReturn(store);

        when(employeeService.getEntityById(employeeId)).thenReturn(employee);

        when(repository.save(employeeProposalDaysOff)).thenReturn(employeeProposalDaysOff);

        ResponseEmployeeProposalDaysOffDTO responseEmployeeProposalDaysOffDTO = new TestResponseEmployeeProposalDaysOffDTO()
                .withStoreId(storeId)
                .withEmployeeId(employeeId)
                .withMonthlyDaysOff(monthlyDaysOff)
                .withYear(year)
                .withMonth(month).build();

        when(mapper.toResponseEmployeeProposalDaysOffDTO(employeeProposalDaysOff)).thenReturn(responseEmployeeProposalDaysOffDTO);

        UpdateEmployeeProposalDaysOffDTO dto =
                new TestUpdateEmployeeProposalDaysOffDTO()
                        .withYear(year)
                        .withMonth(month)
                        .withMonthlyDaysOff(monthlyDaysOff)
                        .build();
        //when
        ResponseEmployeeProposalDaysOffDTO serviceResponse = service.updateEmployeeProposalDaysOff(storeId, employeeId, dto);

        //then
        assertEquals(storeId,serviceResponse.storeId());
        assertEquals(employeeId,serviceResponse.employeeId());
        assertEquals(year,serviceResponse.year());
        assertEquals(month,serviceResponse.month());
        assertEquals(monthlyDaysOff,serviceResponse.monthlyDaysOff());

        verify(mapper,times(1)).updateEmployeeProposalDaysOff(dto,employeeProposalDaysOff);
        verify(repository,times(1)).save(employeeProposalDaysOff);
    }

    @Test
    void updateEmployeeProposalDaysOff_loggedUserHasNotAccessToStoreThrowsException(){
        //given
        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(false);

        UpdateEmployeeProposalDaysOffDTO dto = new TestUpdateEmployeeProposalDaysOffDTO().build();
        //when
        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> service.updateEmployeeProposalDaysOff(storeId, employeeId, dto));

        //then
        assertEquals("Access denied for store with id " + storeId,exception.getMessage());

        verify(repository,never()).findByStoreIdAndEmployeeIdAndYearAndMonth(any(),any(),any(),any());
        verify(storeService,never()).getEntityById(any());
        verify(employeeService,never()).getEntityById(any());
        verify(mapper,never()).updateEmployeeProposalDaysOff(any(),any());
        verify(repository,never()).save(any());
        verify(mapper,never()).toResponseEmployeeProposalDaysOffDTO(any());
    }

    @Test
    void updateEmployeeProposalDaysOff_employeeProposalDaysOffDoesNotExistThrowsException(){
        //given
        Integer year = 2025;
        Integer month = 12;

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(repository.findByStoreIdAndEmployeeIdAndYearAndMonth(storeId,employeeId,year,month))
                .thenReturn(Optional.empty());

        UpdateEmployeeProposalDaysOffDTO dto = new TestUpdateEmployeeProposalDaysOffDTO()
                .withYear(year)
                .withMonth(month)
                .build();

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> service.updateEmployeeProposalDaysOff(storeId, employeeId, dto));

        //then
        assertEquals("Cannot find employee proposal days off for year " + year + " and month " + month,exception.getMessage());

        verify(userAuthorizationService,times(1)).hasAccessToStore(storeId);
        verify(storeService,never()).getEntityById(any());
        verify(employeeService,never()).getEntityById(any());
        verify(mapper,never()).updateEmployeeProposalDaysOff(any(),any());
        verify(repository,never()).save(any());
        verify(mapper,never()).toResponseEmployeeProposalDaysOffDTO(any());
    }

    @Test
    void updateEmployeeProposalDaysOff_storeDoesNotExistThrowsException(){
        //given
        Integer year = 2025;
        Integer month = 12;

        EmployeeProposalDaysOff employeeProposalDaysOff = new TestEmployeeProposalDaysOffBuilder()
                .withYear(year)
                .withMonth(month)
                .build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(repository.findByStoreIdAndEmployeeIdAndYearAndMonth(storeId,employeeId,year,month))
                .thenReturn(Optional.of(employeeProposalDaysOff));

        when(storeService.getEntityById(storeId)).thenThrow(EntityNotFoundException.class);

        UpdateEmployeeProposalDaysOffDTO dto = new TestUpdateEmployeeProposalDaysOffDTO()
                .withYear(year)
                .withMonth(month)
                .build();

        //when
        assertThrows(EntityNotFoundException.class, () -> service.updateEmployeeProposalDaysOff(storeId, employeeId, dto));

        //then
        verify(userAuthorizationService,times(1)).hasAccessToStore(storeId);
        verify(repository,times(1)).findByStoreIdAndEmployeeIdAndYearAndMonth(storeId,employeeId,year,month);
        verify(employeeService,never()).getEntityById(any());
        verify(mapper,never()).updateEmployeeProposalDaysOff(any(),any());
        verify(repository,never()).save(any());
        verify(mapper,never()).toResponseEmployeeProposalDaysOffDTO(any());
    }

    @Test
    void updateEmployeeProposalDaysOff_employeeDoesNotExistThrowsException(){
        //given
        Store store = new TestStoreBuilder().build();
        Integer year = 2025;
        Integer month = 12;

        EmployeeProposalDaysOff employeeProposalDaysOff = new TestEmployeeProposalDaysOffBuilder()
                .withStore(store)
                .withYear(year)
                .withMonth(month)
                .build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(repository.findByStoreIdAndEmployeeIdAndYearAndMonth(storeId,employeeId,year,month))
                .thenReturn(Optional.of(employeeProposalDaysOff));

        when(storeService.getEntityById(storeId)).thenReturn(store);

        when(employeeService.getEntityById(employeeId)).thenThrow(EntityNotFoundException.class);

        UpdateEmployeeProposalDaysOffDTO dto = new TestUpdateEmployeeProposalDaysOffDTO()
                .withYear(year)
                .withMonth(month)
                .build();

        //when
        assertThrows(EntityNotFoundException.class, () -> service.updateEmployeeProposalDaysOff(storeId, employeeId, dto));

        //then
        verify(userAuthorizationService,times(1)).hasAccessToStore(storeId);
        verify(repository,times(1)).findByStoreIdAndEmployeeIdAndYearAndMonth(storeId,employeeId,year,month);
        verify(storeService,times(1)).getEntityById(storeId);
        verify(mapper,never()).updateEmployeeProposalDaysOff(any(),any());
        verify(repository,never()).save(any());
        verify(mapper,never()).toResponseEmployeeProposalDaysOffDTO(any());
    }

    @Test
    void updateEmployeeProposalDaysOff_employeeDoesNotBelongToStoreThrowsException(){
        //given
        Store store = new TestStoreBuilder().build();
        Store differentStore = new TestStoreBuilder().build();
        Employee employee = new TestEmployeeBuilder().withStore(differentStore).build();
        Integer year = 2025;
        Integer month = 12;

        EmployeeProposalDaysOff employeeProposalDaysOff = new TestEmployeeProposalDaysOffBuilder()
                .withStore(store)
                .withEmployee(employee)
                .withYear(year)
                .withMonth(month)
                .build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(repository.findByStoreIdAndEmployeeIdAndYearAndMonth(storeId,employeeId,year,month))
                .thenReturn(Optional.of(employeeProposalDaysOff));

        when(storeService.getEntityById(storeId)).thenReturn(store);

        when(employeeService.getEntityById(employeeId)).thenReturn(employee);

        UpdateEmployeeProposalDaysOffDTO dto = new TestUpdateEmployeeProposalDaysOffDTO()
                .withYear(year)
                .withMonth(month)
                .build();

        //when
        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> service.updateEmployeeProposalDaysOff(storeId, employeeId, dto));

        //then
        assertEquals("Employee with ID " + employee.getId() + " does not belong to store with ID " + store.getId(),exception.getMessage());

        verify(userAuthorizationService,times(1)).hasAccessToStore(storeId);
        verify(repository,times(1)).findByStoreIdAndEmployeeIdAndYearAndMonth(storeId,employeeId,year,month);
        verify(storeService,times(1)).getEntityById(storeId);
        verify(employeeService,times(1)).getEntityById(employeeId);
        verify(mapper,never()).updateEmployeeProposalDaysOff(any(),any());
        verify(repository,never()).save(any());
        verify(mapper,never()).toResponseEmployeeProposalDaysOffDTO(any());
    }

    @Test
    void save_workingTest(){
        //given
        Store store = new TestStoreBuilder().build();
        Employee employee = new TestEmployeeBuilder().withStore(store).build();
        Integer year = 2025;
        Integer month = 12;
        int[] monthlyDaysOff = {1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1};

        EmployeeProposalDaysOff employeeProposalDaysOff = new TestEmployeeProposalDaysOffBuilder()
                .withStore(store)
                .withEmployee(employee)
                .withYear(year)
                .withMonth(month)
                .withMontlyDaysOff(monthlyDaysOff).build();

        when(repository.save(employeeProposalDaysOff)).thenReturn(employeeProposalDaysOff);

        ResponseEmployeeProposalDaysOffDTO responseEmployeeProposalDaysOffDTO = new TestResponseEmployeeProposalDaysOffDTO()
                .withStoreId(storeId)
                .withEmployeeId(employeeId)
                .withMonthlyDaysOff(monthlyDaysOff)
                .withYear(year)
                .withMonth(month).build();

        when(mapper.toResponseEmployeeProposalDaysOffDTO(employeeProposalDaysOff)).thenReturn(responseEmployeeProposalDaysOffDTO);

        //when
        ResponseEmployeeProposalDaysOffDTO serviceResponse = service.save(employeeProposalDaysOff);

        //then
        assertEquals(storeId,serviceResponse.storeId());
        assertEquals(employeeId,serviceResponse.employeeId());
        assertEquals(year,serviceResponse.year());
        assertEquals(month,serviceResponse.month());
        assertEquals(monthlyDaysOff,serviceResponse.monthlyDaysOff());

        verify(repository,times(1)).save(employeeProposalDaysOff);
        verify(mapper,times(1)).toResponseEmployeeProposalDaysOffDTO(employeeProposalDaysOff);
    }

    @Test
    void delete_workingTest(){
        //given
        Long employeeProposalDaysOffId = 5L;

        EmployeeProposalDaysOff employeeProposalDaysOff = new TestEmployeeProposalDaysOffBuilder().build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(repository.findById(employeeProposalDaysOffId)).thenReturn(Optional.of(employeeProposalDaysOff));

        //when
        service.delete(storeId, employeeId, employeeProposalDaysOffId);

        //then
        verify(userAuthorizationService,times(1)).hasAccessToStore(storeId);
        verify(repository,times(1)).findById(employeeProposalDaysOffId);
        verify(repository,times(1)).delete(employeeProposalDaysOff);
    }

    @Test
    void delete_loggedUserHasNotAccessToStoreThrowsException(){
        //given
        Long employeeProposalDaysOffId = 5L;

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(false);

        //when
        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> service.delete(storeId, employeeId, employeeProposalDaysOffId));

        //then
        assertEquals("Access denied for store with id " + storeId,exception.getMessage());

        verify(repository,never()).findById(any());
        verify(repository,never()).delete((EmployeeProposalDaysOff) any());
    }

    @Test
    void delete_employeeProposalDaysOffDoesNotExistThrowsException(){
        //given
        Long employeeProposalDaysOffId = 5L;

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(repository.findById(employeeProposalDaysOffId)).thenReturn(Optional.empty());

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> service.delete(storeId, employeeId, employeeProposalDaysOffId));

        //then
        assertEquals("Cannot find employee proposal days off by id " + employeeProposalDaysOffId,exception.getMessage());

        verify(userAuthorizationService,times(1)).hasAccessToStore(storeId);
        verify(repository,times(1)).findById(employeeProposalDaysOffId);
        verify(repository,never()).delete((EmployeeProposalDaysOff) any());
    }

    @Test
    void findById_workingTest(){
        //given
        Long employeeProposalDaysOffId = 5L;
        Integer year = 2025;
        Integer month = 12;
        int[] monthlyDaysOff = {1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1};

        EmployeeProposalDaysOff employeeProposalDaysOff = new TestEmployeeProposalDaysOffBuilder()
                .withYear(year)
                .withMonth(month)
                .withMontlyDaysOff(monthlyDaysOff).build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(repository.findById(employeeProposalDaysOffId)).thenReturn(Optional.of(employeeProposalDaysOff));

        ResponseEmployeeProposalDaysOffDTO responseEmployeeProposalDaysOffDTO = new TestResponseEmployeeProposalDaysOffDTO()
                .withStoreId(storeId)
                .withEmployeeId(employeeId)
                .withMonthlyDaysOff(monthlyDaysOff)
                .withYear(year)
                .withMonth(month).build();

        when(mapper.toResponseEmployeeProposalDaysOffDTO(employeeProposalDaysOff)).thenReturn(responseEmployeeProposalDaysOffDTO);

        //when
        ResponseEmployeeProposalDaysOffDTO serviceResponse = service.findById(storeId, employeeId, employeeProposalDaysOffId);

        //then
        assertEquals(storeId,serviceResponse.storeId());
        assertEquals(employeeId,serviceResponse.employeeId());
        assertEquals(year,serviceResponse.year());
        assertEquals(month,serviceResponse.month());
        assertEquals(monthlyDaysOff,serviceResponse.monthlyDaysOff());

        verify(userAuthorizationService,times(1)).hasAccessToStore(storeId);
        verify(repository,times(1)).findById(employeeProposalDaysOffId);
        verify(mapper,times(1)).toResponseEmployeeProposalDaysOffDTO(employeeProposalDaysOff);
    }

    @Test
    void findById_loggedUserHasNotAccessToStoreThrowsException(){
        //given
        Long employeeProposalDaysOffId = 5L;

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(false);

        //when
        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> service.findById(storeId, employeeId, employeeProposalDaysOffId));

        //then
        assertEquals("Access denied for store with id " + storeId,exception.getMessage());

        verify(repository,never()).findById(any());
        verify(mapper,never()).toResponseEmployeeProposalDaysOffDTO(any());
    }

    @Test
    void findById_employeeProposalDaysOffDoesNotExistThrowsException(){
        //given
        Long employeeProposalDaysOffId = 5L;

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(repository.findById(employeeProposalDaysOffId)).thenReturn(Optional.empty());

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> service.findById(storeId, employeeId, employeeProposalDaysOffId));

        //then
        assertEquals("Cannot find employee proposal days off by id " + employeeProposalDaysOffId,exception.getMessage());

        verify(userAuthorizationService,times(1)).hasAccessToStore(storeId);
        verify(repository,times(1)).findById(employeeProposalDaysOffId);
        verify(mapper,never()).toResponseEmployeeProposalDaysOffDTO(any());
    }

    @Test
    void exists_returnsTrueWhenExists(){
        //given
        Long employeeProposalDaysOffId = 5L;

        when(repository.existsById(employeeProposalDaysOffId)).thenReturn(true);

        //when
        boolean result = service.exists(employeeProposalDaysOffId);

        //then
        assertTrue(result);

        verify(repository,times(1)).existsById(employeeProposalDaysOffId);
    }

    @Test
    void exists_returnsFalseWhenDoesNotExist(){
        //given
        Long employeeProposalDaysOffId = 5L;

        when(repository.existsById(employeeProposalDaysOffId)).thenReturn(false);

        //when
        boolean result = service.exists(employeeProposalDaysOffId);

        //then
        assertFalse(result);

        verify(repository,times(1)).existsById(employeeProposalDaysOffId);
    }
}