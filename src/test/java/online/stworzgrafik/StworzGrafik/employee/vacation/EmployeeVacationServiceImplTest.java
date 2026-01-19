package online.stworzgrafik.StworzGrafik.employee.vacation;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.employee.TestEmployeeBuilder;
import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.CreateEmployeeVacationDTO;
import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.EmployeeVacationSpecificationDTO;
import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.ResponseEmployeeVacationDTO;
import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.UpdateEmployeeVacationDTO;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeVacationServiceImplTest {
    @InjectMocks
    private EmployeeVacationServiceImpl service;

    @Mock
    private EmployeeVacationBuilder builder;

    @Mock
    private EmployeeVacationMapper mapper;

    @Mock
    private EmployeeVacationRepository repository;

    @Mock
    private UserAuthorizationService userAuthorizationService;

    @Mock
    private StoreEntityService storeService;

    @Mock
    private EmployeeEntityService employeeService;

    private Long storeId = 1L;
    private Long employeeId = 9L;
    private Long employeeVacationId = 21L;
    private Store store;
    private Employee employee;

    @BeforeEach
    void setup() {
        store = new TestStoreBuilder().build();
        employee = new TestEmployeeBuilder().withStore(store).build();
    }

    @Test
    void createEmployeeProposalVacation_workingTest() {
        //given
        Store store = new TestStoreBuilder().build();
        Employee employee = new TestEmployeeBuilder().withStore(store).build();
        Integer year = 2025;
        Integer month = 12;
        int[] monthlyVacation = {1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1};

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(storeService.getEntityById(storeId)).thenReturn(store);

        when(employeeService.getEntityById(employeeId)).thenReturn(employee);

        when(repository.existsByStore_IdAndEmployee_IdAndYearAndMonth(storeId, employeeId, year, month)).thenReturn(false);

        EmployeeVacation employeeVacation = new TestEmployeeVacationBuilder()
                .withStore(store)
                .withEmployee(employee)
                .withYear(year)
                .withMonth(month)
                .withMonthlyVacation(monthlyVacation).build();

        when(builder.createEmployeeVacation(store, employee, year, month, monthlyVacation)).thenReturn(employeeVacation);

        ResponseEmployeeVacationDTO responseEmployeeVacationDTO = new TestResponseEmployeeVacationDTO()
                .withStoreId(storeId)
                .withEmployeeId(employeeId)
                .withMonthlyVacation(monthlyVacation)
                .withYear(year)
                .withMonth(month).build();

        when(repository.save(employeeVacation)).thenReturn(employeeVacation);

        when(mapper.toResponseEmployeeVacationDTO(employeeVacation)).thenReturn(responseEmployeeVacationDTO);

        CreateEmployeeVacationDTO dto =
                new TestCreateEmployeeVacationDTO()
                        .withYear(year)
                        .withMonth(month)
                        .withMonthlyVacation(monthlyVacation)
                        .build();
        //when
        ResponseEmployeeVacationDTO serviceResponse = service.createEmployeeProposalVacation(storeId, employeeId, dto);

        //then
        assertEquals(storeId, serviceResponse.storeId());
        assertEquals(employeeId, serviceResponse.employeeId());
        assertEquals(year, serviceResponse.year());
        assertEquals(month, serviceResponse.month());
        assertEquals(monthlyVacation, serviceResponse.monthlyVacation());
    }

    @Test
    void createEmployeeProposalVacation_loggedUserHasNotAccessToStoreThrowsException() {
        //given
        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(false);

        CreateEmployeeVacationDTO dto = new TestCreateEmployeeVacationDTO().build();
        //when
        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> service.createEmployeeProposalVacation(storeId, employeeId, dto));

        //then
        assertEquals("Access denied for store with id " + storeId, exception.getMessage());

        verify(storeService, never()).getEntityById(any());
        verify(employeeService, never()).getEntityById(any());
        verify(repository, never()).existsByStore_IdAndEmployee_IdAndYearAndMonth(any(), any(), any(), any());
        verify(builder, never()).createEmployeeVacation(any(), any(), any(), any(), any());
        verify(mapper, never()).toResponseEmployeeVacationDTO(any());
    }

    @Test
    void createEmployeeProposalVacation_storeDoesNotExistThrowsException() {
        //given
        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(storeService.getEntityById(storeId)).thenThrow(EntityNotFoundException.class);

        CreateEmployeeVacationDTO dto = new TestCreateEmployeeVacationDTO().build();

        //when
        assertThrows(EntityNotFoundException.class, () -> service.createEmployeeProposalVacation(storeId, employeeId, dto));

        //then
        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(employeeService, never()).getEntityById(storeId);
        verify(repository, never()).existsByStore_IdAndEmployee_IdAndYearAndMonth(any(), any(), any(), any());
        verify(builder, never()).createEmployeeVacation(any(), any(), any(), any(), any());
        verify(mapper, never()).toResponseEmployeeVacationDTO(any());
    }

    @Test
    void createEmployeeProposalVacation_employeeDoesNotExistThrowsException() {
        //given
        Store store = new TestStoreBuilder().build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(storeService.getEntityById(any())).thenReturn(store);

        when(employeeService.getEntityById(any())).thenThrow(EntityNotFoundException.class);

        CreateEmployeeVacationDTO dto = new TestCreateEmployeeVacationDTO().build();

        //when
        assertThrows(EntityNotFoundException.class, () -> service.createEmployeeProposalVacation(storeId, employeeId, dto));

        //then
        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(storeService, times(1)).getEntityById(storeId);
        verify(repository, never()).existsByStore_IdAndEmployee_IdAndYearAndMonth(any(), any(), any(), any());
        verify(builder, never()).createEmployeeVacation(any(), any(), any(), any(), any());
        verify(mapper, never()).toResponseEmployeeVacationDTO(any());
    }

    @Test
    void createEmployeeProposalVacation_employeeDoesNotBelongToStoreThrowsException() {
        //given
        Store store = new TestStoreBuilder().build();
        Store differentStore = new TestStoreBuilder().build();
        Employee employee = new TestEmployeeBuilder().withStore(differentStore).build();
        Integer year = 2025;
        Integer month = 12;

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(storeService.getEntityById(storeId)).thenReturn(store);

        when(employeeService.getEntityById(employeeId)).thenReturn(employee);

        CreateEmployeeVacationDTO dto = new TestCreateEmployeeVacationDTO()
                .withYear(year)
                .withMonth(month)
                .build();

        //when
        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> service.createEmployeeProposalVacation(storeId, employeeId, dto));

        //then
        assertEquals("Employee with ID " + employee.getId() + " does not belong to store with ID " + store.getId(), exception.getMessage());

        verify(userAuthorizationService, times(1)).hasAccessToStore(any());
        verify(storeService, times(1)).getEntityById(any());
        verify(employeeService, times(1)).getEntityById(any());
        verify(repository, never()).existsByStore_IdAndEmployee_IdAndYearAndMonth(any(), any(), any(), any());
        verify(builder, never()).createEmployeeVacation(any(), any(), any(), any(), any());
        verify(mapper, never()).toResponseEmployeeVacationDTO(any());
    }

    @Test
    void createEmployeeProposalVacation_vacationForThisDayAlreadyExistsThrowsException() {
        //given
        Store store = new TestStoreBuilder().build();
        Employee employee = new TestEmployeeBuilder().withStore(store).build();
        Integer year = 2025;
        Integer month = 12;

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(storeService.getEntityById(storeId)).thenReturn(store);

        when(employeeService.getEntityById(employeeId)).thenReturn(employee);

        when(repository.existsByStore_IdAndEmployee_IdAndYearAndMonth(storeId, employeeId, year, month)).thenReturn(true);

        CreateEmployeeVacationDTO dto = new TestCreateEmployeeVacationDTO()
                .withYear(year)
                .withMonth(month)
                .build();

        //when
        EntityExistsException exception =
                assertThrows(EntityExistsException.class, () -> service.createEmployeeProposalVacation(storeId, employeeId, dto));

        //then
        assertEquals("Employee vacation in month " + dto.month() + " of  year " + dto.year() + " already exists", exception.getMessage());

        verify(userAuthorizationService, times(1)).hasAccessToStore(any());
        verify(storeService, times(1)).getEntityById(any());
        verify(employeeService, times(1)).getEntityById(any());
        verify(builder, never()).createEmployeeVacation(any(), any(), any(), any(), any());
        verify(mapper, never()).toResponseEmployeeVacationDTO(any());
    }

    @Test
    void updateEmployeeVacation_workingTest() {
        //given
        Store store = new TestStoreBuilder().build();
        Employee employee = new TestEmployeeBuilder().withStore(store).build();
        Integer year = 2025;
        Integer month = 12;
        int[] monthlyVacation = {1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1};

        EmployeeVacation employeeVacation = new TestEmployeeVacationBuilder()
                .withStore(store)
                .withEmployee(employee)
                .withYear(year)
                .withMonth(month)
                .withMonthlyVacation(monthlyVacation).build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(repository.findById(employeeVacationId))
                .thenReturn(Optional.of(employeeVacation));

        when(storeService.getEntityById(storeId)).thenReturn(store);

        when(employeeService.getEntityById(employeeId)).thenReturn(employee);

        when(repository.save(employeeVacation)).thenReturn(employeeVacation);

        ResponseEmployeeVacationDTO responseEmployeeVacationDTO = new TestResponseEmployeeVacationDTO()
                .withStoreId(storeId)
                .withEmployeeId(employeeId)
                .withMonthlyVacation(monthlyVacation)
                .withYear(year)
                .withMonth(month).build();

        when(mapper.toResponseEmployeeVacationDTO(employeeVacation)).thenReturn(responseEmployeeVacationDTO);

        UpdateEmployeeVacationDTO dto =
                new TestUpdateEmployeeVacationDTO()
                        .withYear(year)
                        .withMonth(month)
                        .withMonthlyVacation(monthlyVacation)
                        .build();
        //when
        ResponseEmployeeVacationDTO serviceResponse = service.updateEmployeeVacation(storeId, employeeId, employeeVacationId, dto);

        //then
        assertEquals(storeId, serviceResponse.storeId());
        assertEquals(employeeId, serviceResponse.employeeId());
        assertEquals(year, serviceResponse.year());
        assertEquals(month, serviceResponse.month());
        assertEquals(monthlyVacation, serviceResponse.monthlyVacation());

        verify(mapper, times(1)).updateEmployeeVacation(dto, employeeVacation);
        verify(repository, times(1)).save(employeeVacation);
    }

    @Test
    void updateEmployeeVacation_loggedUserHasNotAccessToStoreThrowsException() {
        //given
        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(false);

        UpdateEmployeeVacationDTO dto = new TestUpdateEmployeeVacationDTO().build();
        //when
        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> service.updateEmployeeVacation(storeId, employeeId, employeeVacationId, dto));

        //then
        assertEquals("Access denied for store with id " + storeId, exception.getMessage());

        verify(repository, never()).findById(any());
        verify(storeService, never()).getEntityById(any());
        verify(employeeService, never()).getEntityById(any());
        verify(mapper, never()).updateEmployeeVacation(any(), any());
        verify(repository, never()).save(any());
        verify(mapper, never()).toResponseEmployeeVacationDTO(any());
    }

    @Test
    void updateEmployeeVacation_employeeVacationDoesNotExistThrowsException() {
        //given
        Integer year = 2025;
        Integer month = 12;

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(repository.findById(employeeVacationId))
                .thenReturn(Optional.empty());

        UpdateEmployeeVacationDTO dto = new TestUpdateEmployeeVacationDTO()
                .withYear(year)
                .withMonth(month)
                .build();

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> service.updateEmployeeVacation(storeId, employeeId, employeeVacationId, dto));

        //then
        assertEquals("Cannot find employee vacation with id " + employeeVacationId, exception.getMessage());

        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(storeService, never()).getEntityById(any());
        verify(employeeService, never()).getEntityById(any());
        verify(mapper, never()).updateEmployeeVacation(any(), any());
        verify(repository, never()).save(any());
        verify(mapper, never()).toResponseEmployeeVacationDTO(any());
    }

    @Test
    void updateEmployeeVacation_storeDoesNotExistThrowsException() {
        //given
        Integer year = 2025;
        Integer month = 12;

        EmployeeVacation employeeVacation = new TestEmployeeVacationBuilder()
                .withYear(year)
                .withMonth(month)
                .build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(repository.findById(employeeVacationId))
                .thenReturn(Optional.of(employeeVacation));

        when(storeService.getEntityById(storeId)).thenThrow(EntityNotFoundException.class);

        UpdateEmployeeVacationDTO dto = new TestUpdateEmployeeVacationDTO()
                .withYear(year)
                .withMonth(month)
                .build();

        //when
        assertThrows(EntityNotFoundException.class, () -> service.updateEmployeeVacation(storeId, employeeId, employeeVacationId, dto));

        //then
        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(repository, times(1)).findById(employeeVacationId);
        verify(employeeService, never()).getEntityById(any());
        verify(mapper, never()).updateEmployeeVacation(any(), any());
        verify(repository, never()).save(any());
        verify(mapper, never()).toResponseEmployeeVacationDTO(any());
    }

    @Test
    void updateEmployeeVacation_employeeDoesNotExistThrowsException() {
        //given
        Store store = new TestStoreBuilder().build();
        Integer year = 2025;
        Integer month = 12;

        EmployeeVacation employeeVacation = new TestEmployeeVacationBuilder()
                .withStore(store)
                .withYear(year)
                .withMonth(month)
                .build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(repository.findById(employeeVacationId))
                .thenReturn(Optional.of(employeeVacation));

        when(storeService.getEntityById(storeId)).thenReturn(store);

        when(employeeService.getEntityById(employeeId)).thenThrow(EntityNotFoundException.class);

        UpdateEmployeeVacationDTO dto = new TestUpdateEmployeeVacationDTO()
                .withYear(year)
                .withMonth(month)
                .build();

        //when
        assertThrows(EntityNotFoundException.class, () -> service.updateEmployeeVacation(storeId, employeeId, employeeVacationId, dto));

        //then
        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(repository, times(1)).findById(employeeVacationId);
        verify(storeService, times(1)).getEntityById(storeId);
        verify(mapper, never()).updateEmployeeVacation(any(), any());
        verify(repository, never()).save(any());
        verify(mapper, never()).toResponseEmployeeVacationDTO(any());
    }

    @Test
    void updateEmployeeVacation_employeeDoesNotBelongToStoreThrowsException() {
        //given
        Store store = new TestStoreBuilder().build();
        Store differentStore = new TestStoreBuilder().build();
        Employee employee = new TestEmployeeBuilder().withStore(differentStore).build();
        Integer year = 2025;
        Integer month = 12;

        EmployeeVacation employeeVacation = new TestEmployeeVacationBuilder()
                .withStore(store)
                .withEmployee(employee)
                .withYear(year)
                .withMonth(month)
                .build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(repository.findById(employeeVacationId))
                .thenReturn(Optional.of(employeeVacation));

        when(storeService.getEntityById(storeId)).thenReturn(store);

        when(employeeService.getEntityById(employeeId)).thenReturn(employee);

        UpdateEmployeeVacationDTO dto = new TestUpdateEmployeeVacationDTO()
                .withYear(year)
                .withMonth(month)
                .build();

        //when
        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> service.updateEmployeeVacation(storeId, employeeId, employeeVacationId, dto));

        //then
        assertEquals("Employee with ID " + employee.getId() + " does not belong to store with ID " + store.getId(), exception.getMessage());

        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(repository, times(1)).findById(employeeVacationId);
        verify(storeService, times(1)).getEntityById(storeId);
        verify(employeeService, times(1)).getEntityById(employeeId);
        verify(mapper, never()).updateEmployeeVacation(any(), any());
        verify(repository, never()).save(any());
        verify(mapper, never()).toResponseEmployeeVacationDTO(any());
    }

    @Test
    void save_workingTest() {
        //given
        Store store = new TestStoreBuilder().build();
        Employee employee = new TestEmployeeBuilder().withStore(store).build();
        Integer year = 2025;
        Integer month = 12;
        int[] monthlyVacation = {1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1};

        EmployeeVacation employeeVacation = new TestEmployeeVacationBuilder()
                .withStore(store)
                .withEmployee(employee)
                .withYear(year)
                .withMonth(month)
                .withMonthlyVacation(monthlyVacation).build();

        when(repository.save(employeeVacation)).thenReturn(employeeVacation);

        ResponseEmployeeVacationDTO responseEmployeeVacationDTO = new TestResponseEmployeeVacationDTO()
                .withStoreId(storeId)
                .withEmployeeId(employeeId)
                .withMonthlyVacation(monthlyVacation)
                .withYear(year)
                .withMonth(month).build();

        when(mapper.toResponseEmployeeVacationDTO(employeeVacation)).thenReturn(responseEmployeeVacationDTO);

        //when
        ResponseEmployeeVacationDTO serviceResponse = service.save(employeeVacation);

        //then
        assertEquals(storeId, serviceResponse.storeId());
        assertEquals(employeeId, serviceResponse.employeeId());
        assertEquals(year, serviceResponse.year());
        assertEquals(month, serviceResponse.month());
        assertEquals(monthlyVacation, serviceResponse.monthlyVacation());

        verify(repository, times(1)).save(employeeVacation);
        verify(mapper, times(1)).toResponseEmployeeVacationDTO(employeeVacation);
    }

    @Test
    void delete_workingTest() {
        //given
        Long employeeVacationId = 5L;

        EmployeeVacation employeeVacation = new TestEmployeeVacationBuilder().build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(repository.findById(employeeVacationId)).thenReturn(Optional.of(employeeVacation));

        //when
        service.delete(storeId, employeeId, employeeVacationId);

        //then
        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(repository, times(1)).findById(employeeVacationId);
        verify(repository, times(1)).delete(employeeVacation);
    }

    @Test
    void delete_loggedUserHasNotAccessToStoreThrowsException() {
        //given
        Long employeeVacationId = 5L;

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(false);

        //when
        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> service.delete(storeId, employeeId, employeeVacationId));

        //then
        assertEquals("Access denied for store with id " + storeId, exception.getMessage());

        verify(repository, never()).findById(any());
        verify(repository, never()).delete((EmployeeVacation) any());
    }

    @Test
    void delete_employeeVacationDoesNotExistThrowsException() {
        //given
        Long employeeVacationId = 5L;

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(repository.findById(employeeVacationId)).thenReturn(Optional.empty());

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> service.delete(storeId, employeeId, employeeVacationId));

        //then
        assertEquals("Cannot find employee vacation by id " + employeeVacationId, exception.getMessage());

        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(repository, times(1)).findById(employeeVacationId);
        verify(repository, never()).delete((EmployeeVacation) any());
    }

    @Test
    void getById_workingTest() {
        //given
        Long employeeVacationId = 5L;
        Integer year = 2025;
        Integer month = 12;
        int[] monthlyVacation = {1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1};

        EmployeeVacation employeeVacation = new TestEmployeeVacationBuilder()
                .withYear(year)
                .withMonth(month)
                .withMonthlyVacation(monthlyVacation).build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(repository.findById(employeeVacationId)).thenReturn(Optional.of(employeeVacation));

        ResponseEmployeeVacationDTO responseEmployeeVacationDTO = new TestResponseEmployeeVacationDTO()
                .withStoreId(storeId)
                .withEmployeeId(employeeId)
                .withMonthlyVacation(monthlyVacation)
                .withYear(year)
                .withMonth(month).build();

        when(mapper.toResponseEmployeeVacationDTO(employeeVacation)).thenReturn(responseEmployeeVacationDTO);

        //when
        ResponseEmployeeVacationDTO serviceResponse = service.getById(storeId, employeeId, employeeVacationId);

        //then
        assertEquals(storeId, serviceResponse.storeId());
        assertEquals(employeeId, serviceResponse.employeeId());
        assertEquals(year, serviceResponse.year());
        assertEquals(month, serviceResponse.month());
        assertEquals(monthlyVacation, serviceResponse.monthlyVacation());

        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(repository, times(1)).findById(employeeVacationId);
        verify(mapper, times(1)).toResponseEmployeeVacationDTO(employeeVacation);
    }

    @Test
    void getById_loggedUserHasNotAccessToStoreThrowsException() {
        //given
        Long employeeVacationId = 5L;

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(false);

        //when
        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> service.getById(storeId, employeeId, employeeVacationId));

        //then
        assertEquals("Access denied for store with id " + storeId, exception.getMessage());

        verify(repository, never()).findById(any());
        verify(mapper, never()).toResponseEmployeeVacationDTO(any());
    }

    @Test
    void getById_employeeVacationDoesNotExistThrowsException() {
        //given
        Long employeeVacationId = 5L;

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(repository.findById(employeeVacationId)).thenReturn(Optional.empty());

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> service.getById(storeId, employeeId, employeeVacationId));

        //then
        assertEquals("Cannot find employee vacation by id " + employeeVacationId, exception.getMessage());

        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(repository, times(1)).findById(employeeVacationId);
        verify(mapper, never()).toResponseEmployeeVacationDTO(any());
    }

    @Test
    void getByCriteria_workingTest() {
        //given
        Integer year = 2025;
        Integer month = 12;

        EmployeeVacation vacation1 = new TestEmployeeVacationBuilder()
                .withYear(year)
                .withMonth(month)
                .build();

        EmployeeVacation vacation2 = new TestEmployeeVacationBuilder()
                .withYear(year)
                .withMonth(month)
                .build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(repository.findAll(any(Specification.class))).thenReturn(List.of(vacation1, vacation2));

        ResponseEmployeeVacationDTO response1 = new TestResponseEmployeeVacationDTO()
                .withStoreId(storeId)
                .withEmployeeId(employeeId)
                .withYear(year)
                .withMonth(month)
                .build();

        ResponseEmployeeVacationDTO response2 = new TestResponseEmployeeVacationDTO()
                .withStoreId(storeId)
                .withEmployeeId(employeeId)
                .withYear(year)
                .withMonth(month)
                .build();

        when(mapper.toResponseEmployeeVacationDTO(vacation1)).thenReturn(response1);
        when(mapper.toResponseEmployeeVacationDTO(vacation2)).thenReturn(response2);

        EmployeeVacationSpecificationDTO dto = new TestEmployeeVacationSpecificationDTO().withEmployeeId(employeeId).withYear(year).withMonth(month).build();

        //when
        List<ResponseEmployeeVacationDTO> result = service.getByCriteria(storeId, dto);

        //then
        assertEquals(2, result.size());
        assertEquals(response1, result.get(0));
        assertEquals(response2, result.get(1));

        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(repository, times(1)).findAll(any(Specification.class));
        verify(mapper, times(2)).toResponseEmployeeVacationDTO(any());
    }

    @Test
    void getByCriteria_loggedUserHasNotAccessToStoreThrowsException() {
        //given
        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(false);

        EmployeeVacationSpecificationDTO dto = new TestEmployeeVacationSpecificationDTO().withEmployeeId(employeeId).withYear(2025).withMonth(12).build();

        //when
        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> service.getByCriteria(storeId, dto));

        //then
        assertEquals("Access denied for store with id " + storeId, exception.getMessage());

        verify(repository, never()).findAll(any(Specification.class));
        verify(mapper, never()).toResponseEmployeeVacationDTO(any());
    }

    @Test
    void exists_returnsTrueWhenExists() {
        //given
        Long employeeVacationId = 5L;

        when(repository.existsById(employeeVacationId)).thenReturn(true);

        //when
        boolean result = service.exists(employeeVacationId);

        //then
        assertTrue(result);

        verify(repository, times(1)).existsById(employeeVacationId);
    }

    @Test
    void exists_returnsFalseWhenDoesNotExist() {
        //given
        Long employeeVacationId = 5L;

        when(repository.existsById(employeeVacationId)).thenReturn(false);

        //when
        boolean result = service.exists(employeeVacationId);

        //then
        assertFalse(result);

        verify(repository, times(1)).existsById(employeeVacationId);
    }
}