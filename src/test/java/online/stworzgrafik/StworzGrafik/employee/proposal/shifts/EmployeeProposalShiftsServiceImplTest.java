package online.stworzgrafik.StworzGrafik.employee.proposal.shifts;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PrePersist;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.employee.TestEmployeeBuilder;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO.CreateEmployeeProposalShiftsDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO.ResponseEmployeeProposalShiftsDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO.UpdateEmployeeProposalShiftsDTO;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeProposalShiftsServiceImplTest {
    @InjectMocks
    private EmployeeProposalShiftsServiceImpl service;

    @Mock
    private EmployeeProposalShiftsBuilder builder;

    @Mock
    private EmployeeProposalShiftsMapper mapper;

    @Mock
    private EmployeeProposalShiftsRepository repository;

    @Mock
    private UserAuthorizationService userAuthorizationService;

    @Mock
    private StoreEntityService storeService;

    @Mock
    private EmployeeEntityService employeeService;

    private Long storeId = 1L;
    private Long employeeId = 9L;
    private Long employeeProposalShiftId = 21L;
    private Store store;
    private Employee employee;

    @PrePersist
    void setup(){
        store = new TestStoreBuilder().build();
        employee = new TestEmployeeBuilder().withStore(store).build();
    }

    @Test
    void createEmployeeProposalShift_workingTest(){
        //given
        Store store = new TestStoreBuilder().build();
        Employee employee = new TestEmployeeBuilder().withStore(store).build();
        LocalDate date = LocalDate.of(2025, 12, 15);
        int[] dailyProposalShift = {0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0};

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(storeService.getEntityById(storeId)).thenReturn(store);

        when(employeeService.getEntityById(employeeId)).thenReturn(employee);

        when(repository.existsByStore_IdAndEmployee_IdAndDate(storeId, employeeId, date)).thenReturn(false);

        EmployeeProposalShifts employeeProposalShifts = new TestEmployeeProposalShiftsBuilder()
                .withStore(store)
                .withEmployee(employee)
                .withDate(date)
                .withDailyProposalShift(dailyProposalShift).build();

        when(builder.createEmployeeProposalShifts(store, employee, date, dailyProposalShift)).thenReturn(employeeProposalShifts);

        ResponseEmployeeProposalShiftsDTO responseEmployeeProposalShiftsDTO = new TestResponseEmployeeProposalShiftsDTO()
                .withStoreId(storeId)
                .withEmployeeId(employeeId)
                .withDate(date)
                .withDailyProposalShift(dailyProposalShift).build();

        when(repository.save(employeeProposalShifts)).thenReturn(employeeProposalShifts);

        when(mapper.toResponseEmployeeProposalShiftsDTO(employeeProposalShifts)).thenReturn(responseEmployeeProposalShiftsDTO);

        CreateEmployeeProposalShiftsDTO dto =
                new TestCreateEmployeeProposalShiftsDTO()
                        .withDate(date)
                        .withDailyProposalShift(dailyProposalShift)
                        .build();
        //when
        ResponseEmployeeProposalShiftsDTO serviceResponse = service.createEmployeeProposalShift(storeId, employeeId, dto);

        //then
        assertEquals(storeId, serviceResponse.storeId());
        assertEquals(employeeId, serviceResponse.employeeId());
        assertEquals(date, serviceResponse.date());
        assertEquals(dailyProposalShift, serviceResponse.dailyProposalShift());
    }

    @Test
    void createEmployeeProposalShift_loggedUserHasNotAccessToStoreThrowsException(){
        //given
        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(false);

        CreateEmployeeProposalShiftsDTO dto = new TestCreateEmployeeProposalShiftsDTO().build();
        //when
        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> service.createEmployeeProposalShift(storeId, employeeId, dto));

        //then
        assertEquals("Access denied for store with id " + storeId, exception.getMessage());

        verify(storeService, never()).getEntityById(any());
        verify(employeeService, never()).getEntityById(any());
        verify(repository, never()).existsByStore_IdAndEmployee_IdAndDate(any(), any(), any());
        verify(builder, never()).createEmployeeProposalShifts(any(), any(), any(), any());
        verify(mapper, never()).toResponseEmployeeProposalShiftsDTO(any());
    }

    @Test
    void createEmployeeProposalShift_storeDoesNotExistThrowsException(){
        //given
        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(storeService.getEntityById(storeId)).thenThrow(EntityNotFoundException.class);

        CreateEmployeeProposalShiftsDTO dto = new TestCreateEmployeeProposalShiftsDTO().build();

        //when
        assertThrows(EntityNotFoundException.class, () -> service.createEmployeeProposalShift(storeId, employeeId, dto));

        //then
        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(employeeService, never()).getEntityById(storeId);
        verify(repository, never()).existsByStore_IdAndEmployee_IdAndDate(any(), any(), any());
        verify(builder, never()).createEmployeeProposalShifts(any(), any(), any(), any());
        verify(mapper, never()).toResponseEmployeeProposalShiftsDTO(any());
    }

    @Test
    void createEmployeeProposalShift_employeeDoesNotExistThrowsException(){
        //given
        Store store = new TestStoreBuilder().build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(storeService.getEntityById(any())).thenReturn(store);

        when(employeeService.getEntityById(any())).thenThrow(EntityNotFoundException.class);

        CreateEmployeeProposalShiftsDTO dto = new TestCreateEmployeeProposalShiftsDTO().build();

        //when
        assertThrows(EntityNotFoundException.class, () -> service.createEmployeeProposalShift(storeId, employeeId, dto));

        //then
        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(storeService, times(1)).getEntityById(storeId);
        verify(repository, never()).existsByStore_IdAndEmployee_IdAndDate(any(), any(), any());
        verify(builder, never()).createEmployeeProposalShifts(any(), any(), any(), any());
        verify(mapper, never()).toResponseEmployeeProposalShiftsDTO(any());
    }

    @Test
    void createEmployeeProposalShift_employeeDoesNotBelongToStoreThrowsException(){
        //given
        Store store = new TestStoreBuilder().build();
        Store differentStore = new TestStoreBuilder().build();
        Employee employee = new TestEmployeeBuilder().withStore(differentStore).build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(storeService.getEntityById(storeId)).thenReturn(store);

        when(employeeService.getEntityById(employeeId)).thenReturn(employee);

        CreateEmployeeProposalShiftsDTO dto = new TestCreateEmployeeProposalShiftsDTO().build();

        //when
        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> service.createEmployeeProposalShift(storeId, employeeId, dto));

        //then
        assertEquals("Employee with ID " + employee.getId() + " does not belong to store with ID " + store.getId(), exception.getMessage());

        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(storeService, times(1)).getEntityById(storeId);
        verify(employeeService, times(1)).getEntityById(employeeId);
        verify(repository, never()).existsByStore_IdAndEmployee_IdAndDate(any(), any(), any());
        verify(builder, never()).createEmployeeProposalShifts(any(), any(), any(), any());
        verify(mapper, never()).toResponseEmployeeProposalShiftsDTO(any());
    }

    @Test
    void createEmployeeProposalShift_proposalForThisDayAlreadyExistsThrowsException(){
        //given
        Store store = new TestStoreBuilder().build();
        Employee employee = new TestEmployeeBuilder().withStore(store).build();
        LocalDate date = LocalDate.of(2025, 12, 15);

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(storeService.getEntityById(storeId)).thenReturn(store);

        when(employeeService.getEntityById(employeeId)).thenReturn(employee);

        when(repository.existsByStore_IdAndEmployee_IdAndDate(storeId, employeeId, date)).thenReturn(true);

        CreateEmployeeProposalShiftsDTO dto = new TestCreateEmployeeProposalShiftsDTO()
                .withDate(date)
                .build();

        //when
        EntityExistsException exception =
                assertThrows(EntityExistsException.class, () -> service.createEmployeeProposalShift(storeId, employeeId, dto));

        //then
        assertEquals("Employee with ID " + employeeId + " proposal shift for date " + dto.date() + " already exists", exception.getMessage());

        verify(userAuthorizationService, times(1)).hasAccessToStore(any());
        verify(storeService, times(1)).getEntityById(any());
        verify(employeeService, times(1)).getEntityById(any());
        verify(builder, never()).createEmployeeProposalShifts(any(), any(), any(), any());
        verify(mapper, never()).toResponseEmployeeProposalShiftsDTO(any());
    }

    @Test
    void updateEmployeeProposalShift_workingTest(){
        //given
        Store store = new TestStoreBuilder().build();
        Employee employee = new TestEmployeeBuilder().withStore(store).build();
        LocalDate date = LocalDate.of(2025, 12, 15);
        int[] dailyProposalShift = {0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0};

        EmployeeProposalShifts employeeProposalShifts = new TestEmployeeProposalShiftsBuilder()
                .withStore(store)
                .withEmployee(employee)
                .withDate(date)
                .withDailyProposalShift(dailyProposalShift).build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(repository.findById(employeeProposalShiftId))
                .thenReturn(Optional.of(employeeProposalShifts));

        when(storeService.getEntityById(storeId)).thenReturn(store);

        when(employeeService.getEntityById(employeeId)).thenReturn(employee);

        when(repository.save(employeeProposalShifts)).thenReturn(employeeProposalShifts);

        ResponseEmployeeProposalShiftsDTO responseEmployeeProposalShiftsDTO = new TestResponseEmployeeProposalShiftsDTO()
                .withStoreId(storeId)
                .withEmployeeId(employeeId)
                .withDate(date)
                .withDailyProposalShift(dailyProposalShift).build();

        when(mapper.toResponseEmployeeProposalShiftsDTO(employeeProposalShifts)).thenReturn(responseEmployeeProposalShiftsDTO);

        UpdateEmployeeProposalShiftsDTO dto =
                new TestUpdateEmployeeProposalShiftsDTO()
                        .withDate(date)
                        .withDailyProposalShift(dailyProposalShift)
                        .build();
        //when
        ResponseEmployeeProposalShiftsDTO serviceResponse = service.updateEmployeeProposalShift(storeId, employeeId, employeeProposalShiftId, dto);

        //then
        assertEquals(storeId, serviceResponse.storeId());
        assertEquals(employeeId, serviceResponse.employeeId());
        assertEquals(date, serviceResponse.date());
        assertEquals(dailyProposalShift, serviceResponse.dailyProposalShift());

        verify(mapper, times(1)).updateEmployeeProposalShifts(dto, employeeProposalShifts);
        verify(repository, times(1)).save(employeeProposalShifts);
    }

    @Test
    void updateEmployeeProposalShift_loggedUserHasNotAccessToStoreThrowsException(){
        //given
        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(false);

        UpdateEmployeeProposalShiftsDTO dto = new TestUpdateEmployeeProposalShiftsDTO().build();
        //when
        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> service.updateEmployeeProposalShift(storeId, employeeId, employeeProposalShiftId, dto));

        //then
        assertEquals("Access denied for store with id " + storeId, exception.getMessage());

        verify(repository, never()).findById(any());
        verify(storeService, never()).getEntityById(any());
        verify(employeeService, never()).getEntityById(any());
        verify(mapper, never()).updateEmployeeProposalShifts(any(), any());
        verify(repository, never()).save(any());
        verify(mapper, never()).toResponseEmployeeProposalShiftsDTO(any());
    }

    @Test
    void updateEmployeeProposalShift_employeeProposalShiftDoesNotExistThrowsException(){
        //given
        LocalDate date = LocalDate.of(2025, 12, 15);

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(repository.findById(employeeProposalShiftId))
                .thenReturn(Optional.empty());

        UpdateEmployeeProposalShiftsDTO dto = new TestUpdateEmployeeProposalShiftsDTO()
                .withDate(date)
                .build();

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> service.updateEmployeeProposalShift(storeId, employeeId, employeeProposalShiftId, dto));

        //then
        assertEquals("Cannot find employee proposal shift by id " + employeeProposalShiftId, exception.getMessage());

        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(storeService, never()).getEntityById(any());
        verify(employeeService, never()).getEntityById(any());
        verify(mapper, never()).updateEmployeeProposalShifts(any(), any());
        verify(repository, never()).save(any());
        verify(mapper, never()).toResponseEmployeeProposalShiftsDTO(any());
    }

    @Test
    void updateEmployeeProposalShift_storeDoesNotExistThrowsException(){
        //given
        LocalDate date = LocalDate.of(2025, 12, 15);

        EmployeeProposalShifts employeeProposalShifts = new TestEmployeeProposalShiftsBuilder()
                .withDate(date)
                .build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(repository.findById(employeeProposalShiftId))
                .thenReturn(Optional.of(employeeProposalShifts));

        when(storeService.getEntityById(storeId)).thenThrow(EntityNotFoundException.class);

        UpdateEmployeeProposalShiftsDTO dto = new TestUpdateEmployeeProposalShiftsDTO()
                .withDate(date)
                .build();

        //when
        assertThrows(EntityNotFoundException.class, () -> service.updateEmployeeProposalShift(storeId, employeeId, employeeProposalShiftId, dto));

        //then
        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(repository, times(1)).findById(employeeProposalShiftId);
        verify(employeeService, never()).getEntityById(any());
        verify(mapper, never()).updateEmployeeProposalShifts(any(), any());
        verify(repository, never()).save(any());
        verify(mapper, never()).toResponseEmployeeProposalShiftsDTO(any());
    }

    @Test
    void updateEmployeeProposalShift_employeeDoesNotExistThrowsException(){
        //given
        Store store = new TestStoreBuilder().build();
        LocalDate date = LocalDate.of(2025, 12, 15);

        EmployeeProposalShifts employeeProposalShifts = new TestEmployeeProposalShiftsBuilder()
                .withStore(store)
                .withDate(date)
                .build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(repository.findById(employeeProposalShiftId))
                .thenReturn(Optional.of(employeeProposalShifts));

        when(storeService.getEntityById(storeId)).thenReturn(store);

        when(employeeService.getEntityById(employeeId)).thenThrow(EntityNotFoundException.class);

        UpdateEmployeeProposalShiftsDTO dto = new TestUpdateEmployeeProposalShiftsDTO()
                .withDate(date)
                .build();

        //when
        assertThrows(EntityNotFoundException.class, () -> service.updateEmployeeProposalShift(storeId, employeeId, employeeProposalShiftId, dto));

        //then
        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(repository, times(1)).findById(employeeProposalShiftId);
        verify(storeService, times(1)).getEntityById(storeId);
        verify(mapper, never()).updateEmployeeProposalShifts(any(), any());
        verify(repository, never()).save(any());
        verify(mapper, never()).toResponseEmployeeProposalShiftsDTO(any());
    }

    @Test
    void updateEmployeeProposalShift_employeeDoesNotBelongToStoreThrowsException(){
        //given
        Store store = new TestStoreBuilder().build();
        Store differentStore = new TestStoreBuilder().build();
        Employee employee = new TestEmployeeBuilder().withStore(differentStore).build();
        LocalDate date = LocalDate.of(2025, 12, 15);

        EmployeeProposalShifts employeeProposalShifts = new TestEmployeeProposalShiftsBuilder()
                .withStore(store)
                .withEmployee(employee)
                .withDate(date)
                .build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(repository.findById(employeeProposalShiftId))
                .thenReturn(Optional.of(employeeProposalShifts));

        when(storeService.getEntityById(storeId)).thenReturn(store);

        when(employeeService.getEntityById(employeeId)).thenReturn(employee);

        UpdateEmployeeProposalShiftsDTO dto = new TestUpdateEmployeeProposalShiftsDTO()
                .withDate(date)
                .build();

        //when
        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> service.updateEmployeeProposalShift(storeId, employeeId, employeeProposalShiftId, dto));

        //then
        assertEquals("Employee with ID " + employee.getId() + " does not belong to store with ID " + store.getId(), exception.getMessage());

        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(repository, times(1)).findById(employeeProposalShiftId);
        verify(storeService, times(1)).getEntityById(storeId);
        verify(employeeService, times(1)).getEntityById(employeeId);
        verify(mapper, never()).updateEmployeeProposalShifts(any(), any());
        verify(repository, never()).save(any());
        verify(mapper, never()).toResponseEmployeeProposalShiftsDTO(any());
    }

    @Test
    void save_workingTest(){
        //given
        Store store = new TestStoreBuilder().build();
        Employee employee = new TestEmployeeBuilder().withStore(store).build();
        LocalDate date = LocalDate.of(2025, 12, 15);
        int[] dailyProposalShift = {0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0};

        EmployeeProposalShifts employeeProposalShifts = new TestEmployeeProposalShiftsBuilder()
                .withStore(store)
                .withEmployee(employee)
                .withDate(date)
                .withDailyProposalShift(dailyProposalShift).build();

        when(repository.save(employeeProposalShifts)).thenReturn(employeeProposalShifts);

        ResponseEmployeeProposalShiftsDTO responseEmployeeProposalShiftsDTO = new TestResponseEmployeeProposalShiftsDTO()
                .withStoreId(storeId)
                .withEmployeeId(employeeId)
                .withDate(date)
                .withDailyProposalShift(dailyProposalShift).build();

        when(mapper.toResponseEmployeeProposalShiftsDTO(employeeProposalShifts)).thenReturn(responseEmployeeProposalShiftsDTO);

        //when
        ResponseEmployeeProposalShiftsDTO serviceResponse = service.save(employeeProposalShifts);

        //then
        assertEquals(storeId, serviceResponse.storeId());
        assertEquals(employeeId, serviceResponse.employeeId());
        assertEquals(date, serviceResponse.date());
        assertEquals(dailyProposalShift, serviceResponse.dailyProposalShift());

        verify(repository, times(1)).save(employeeProposalShifts);
        verify(mapper, times(1)).toResponseEmployeeProposalShiftsDTO(employeeProposalShifts);
    }

    @Test
    void delete_workingTest(){
        //given
        Long employeeProposalShiftId = 5L;

        EmployeeProposalShifts employeeProposalShifts = new TestEmployeeProposalShiftsBuilder().build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(repository.findById(employeeProposalShiftId)).thenReturn(Optional.of(employeeProposalShifts));

        //when
        service.delete(storeId, employeeId, employeeProposalShiftId);

        //then
        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(repository, times(1)).findById(employeeProposalShiftId);
        verify(repository, times(1)).delete(employeeProposalShifts);
    }

    @Test
    void delete_loggedUserHasNotAccessToStoreThrowsException(){
        //given
        Long employeeProposalShiftId = 5L;

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(false);

        //when
        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> service.delete(storeId, employeeId, employeeProposalShiftId));

        //then
        assertEquals("Access denied for store with id " + storeId, exception.getMessage());

        verify(repository, never()).findById(any());
        verify(repository, never()).delete((EmployeeProposalShifts) any());
    }

    @Test
    void delete_employeeProposalShiftDoesNotExistThrowsException(){
        //given
        Long employeeProposalShiftId = 5L;

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(repository.findById(employeeProposalShiftId)).thenReturn(Optional.empty());

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> service.delete(storeId, employeeId, employeeProposalShiftId));

        //then
        assertEquals("Cannot find employee proposal shift by id " + employeeProposalShiftId, exception.getMessage());

        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(repository, times(1)).findById(employeeProposalShiftId);
        verify(repository, never()).delete((EmployeeProposalShifts) any());
    }

    @Test
    void getById_workingTest(){
        //given
        Long employeeProposalShiftId = 5L;
        LocalDate date = LocalDate.of(2025, 12, 15);
        int[] dailyProposalShift = {0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0};

        EmployeeProposalShifts employeeProposalShifts = new TestEmployeeProposalShiftsBuilder()
                .withDate(date)
                .withDailyProposalShift(dailyProposalShift).build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(repository.findById(employeeProposalShiftId)).thenReturn(Optional.of(employeeProposalShifts));

        ResponseEmployeeProposalShiftsDTO responseEmployeeProposalShiftsDTO = new TestResponseEmployeeProposalShiftsDTO()
                .withStoreId(storeId)
                .withEmployeeId(employeeId)
                .withDate(date)
                .withDailyProposalShift(dailyProposalShift).build();

        when(mapper.toResponseEmployeeProposalShiftsDTO(employeeProposalShifts)).thenReturn(responseEmployeeProposalShiftsDTO);

        //when
        ResponseEmployeeProposalShiftsDTO serviceResponse = service.getById(storeId, employeeId, employeeProposalShiftId);

        //then
        assertEquals(storeId, serviceResponse.storeId());
        assertEquals(employeeId, serviceResponse.employeeId());
        assertEquals(date, serviceResponse.date());
        assertEquals(dailyProposalShift, serviceResponse.dailyProposalShift());

        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(repository, times(1)).findById(employeeProposalShiftId);
        verify(mapper, times(1)).toResponseEmployeeProposalShiftsDTO(employeeProposalShifts);
    }

    @Test
    void getById_loggedUserHasNotAccessToStoreThrowsException(){
        //given
        Long employeeProposalShiftId = 5L;

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(false);

        //when
        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> service.getById(storeId, employeeId, employeeProposalShiftId));

        //then
        assertEquals("Access denied for store with id " + storeId, exception.getMessage());

        verify(repository, never()).findById(any());
        verify(mapper, never()).toResponseEmployeeProposalShiftsDTO(any());
    }

    @Test
    void getById_employeeProposalShiftDoesNotExistThrowsException(){
        //given
        Long employeeProposalShiftId = 5L;

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(repository.findById(employeeProposalShiftId)).thenReturn(Optional.empty());

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> service.getById(storeId, employeeId, employeeProposalShiftId));

        //then
        assertEquals("Cannot find employee proposal shift by id " + employeeProposalShiftId, exception.getMessage());

        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(repository, times(1)).findById(employeeProposalShiftId);
        verify(mapper, never()).toResponseEmployeeProposalShiftsDTO(any());
    }

    @Test
    void exists_returnsTrueWhenExists(){
        //given
        Long employeeProposalShiftId = 5L;

        when(repository.existsById(employeeProposalShiftId)).thenReturn(true);

        //when
        boolean result = service.exists(employeeProposalShiftId);

        //then
        assertTrue(result);

        verify(repository, times(1)).existsById(employeeProposalShiftId);
    }

    @Test
    void exists_returnsFalseWhenDoesNotExist(){
        //given
        Long employeeProposalShiftId = 5L;

        when(repository.existsById(employeeProposalShiftId)).thenReturn(false);

        //when
        boolean result = service.exists(employeeProposalShiftId);

        //then
        assertFalse(result);

        verify(repository, times(1)).existsById(employeeProposalShiftId);
    }
}