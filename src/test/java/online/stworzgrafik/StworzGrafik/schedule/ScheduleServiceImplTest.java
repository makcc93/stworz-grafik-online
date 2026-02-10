package online.stworzgrafik.StworzGrafik.schedule;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.schedule.DTO.CreateScheduleDTO;
import online.stworzgrafik.StworzGrafik.schedule.DTO.ResponseScheduleDTO;
import online.stworzgrafik.StworzGrafik.schedule.DTO.ScheduleSpecificationDTO;
import online.stworzgrafik.StworzGrafik.schedule.DTO.UpdateScheduleDTO;
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

import java.util.List;
import java.util.Optional;
import java.util.Arrays;
import java.util.Collections;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceImplTest {
    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private ScheduleMapper scheduleMapper;

    @Mock
    private UserAuthorizationService userAuthorizationService;

    @Mock
    private ScheduleBuilder scheduleBuilder;

    @Mock
    private StoreEntityService storeEntityService;

    @Test
    void findByEntityById_workingTest(){
        //given
        Long scheduleId = 10L;

        Schedule schedule = new TestScheduleBuilder().build();
        Long idOfSchedule = schedule.getId();
        String nameOfSchedule = schedule.getName();
        Integer monthOfSchedule = schedule.getMonth();
        Integer yearOfSchedule = schedule.getYear();

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.ofNullable(schedule));

        when(userAuthorizationService.hasAccessToStore(schedule.getStore().getId())).thenReturn(true);

        //when
        Schedule serviceResponse = scheduleService.findEntityById(scheduleId);

        //then
        assertEquals(idOfSchedule,serviceResponse.getId());
        assertEquals(nameOfSchedule,serviceResponse.getName());
        assertEquals(monthOfSchedule,serviceResponse.getMonth());
        assertEquals(yearOfSchedule,serviceResponse.getYear());
    }

    @Test
    void findEntityById_scheduleByIdDoesNotExistThrowsException(){
        //given
        Long scheduleId = 999L;

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> scheduleService.findEntityById(scheduleId));

        //then
        assertEquals("Cannot find schedule by id " + scheduleId,exception.getMessage());

        verify(scheduleRepository, times(1)).findById(scheduleId);
        verify(userAuthorizationService,never()).hasAccessToStore(any());
    }

    @Test
    void findByEntityById_loggedUserHasNotAccessToThisStoreThrowsException(){
        //given
        Long scheduleId = 54321L;

        Store store = new TestStoreBuilder().build();
        Long storeId = store.getId();

        Schedule schedule = new TestScheduleBuilder().withStore(store).build();

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.ofNullable(schedule));

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(false);

        //when
        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> scheduleService.findEntityById(scheduleId));
        //then
        assertEquals("Access denied for store with id " + storeId, exception.getMessage());
    }

    @Test
    void createSchedule_workingTest(){
        //given
        Long storeId = 1L;
        Integer year = 2024;
        Integer month = 5;
        Long createdByUserId = 100L;
        String name = "NAME";
        ScheduleStatus scheduleStatus = ScheduleStatus.IN_PROGRESS;

        CreateScheduleDTO dto = new TestCreateScheduleDTO().withName(name).withYear(year).withMonth(month).withCreatedByUserId(createdByUserId).build();
        Store store = new TestStoreBuilder().build();

        Schedule schedule = new TestScheduleBuilder().build();
        ResponseScheduleDTO responseDTO = new TestResponseScheduleDTO().withId(schedule.getId()).withName(schedule.getName()).withYear(schedule.getYear()).withMonth(schedule.getMonth()).build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);
        when(scheduleRepository.existsByStoreIdAndYearAndMonth(storeId, year, month)).thenReturn(false);
        when(storeEntityService.getEntityById(storeId)).thenReturn(store);
        when(scheduleMapper.stringToEnum(any())).thenReturn(scheduleStatus);
        when(scheduleBuilder.createSchedule(store, year, month, name, scheduleStatus, createdByUserId)).thenReturn(schedule);
        when(scheduleRepository.save(schedule)).thenReturn(schedule);
        when(scheduleMapper.toDTO(schedule)).thenReturn(responseDTO);

        //when
        ResponseScheduleDTO serviceResponse = scheduleService.createSchedule(storeId, dto);

        //then
        assertNotNull(serviceResponse);
        assertEquals(responseDTO.id(), serviceResponse.id());

        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(scheduleRepository, times(1)).existsByStoreIdAndYearAndMonth(storeId, year, month);
        verify(storeEntityService, times(1)).getEntityById(storeId);
        verify(scheduleBuilder, times(1)).createSchedule(store, year, month,name, scheduleStatus,createdByUserId);
        verify(scheduleRepository, times(1)).save(schedule);
        verify(scheduleMapper, times(1)).toDTO(schedule);
    }

    @Test
    void createSchedule_loggedUserHasNotAccessToThisStoreThrowsException(){
        //given
        Long storeId = 1L;
        CreateScheduleDTO dto = new TestCreateScheduleDTO().build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(false);

        //when
        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> scheduleService.createSchedule(storeId, dto));

        //then
        assertEquals("Access denied for store with id " + storeId, exception.getMessage());

        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(scheduleRepository, never()).existsByStoreIdAndYearAndMonth(any(), any(), any());
        verify(storeEntityService, never()).getEntityById(any());
    }

    @Test
    void createSchedule_scheduleAlreadyExistsForYearAndMonthThrowsException(){
        //given
        Long storeId = 1L;
        Integer year = 2024;
        Integer month = 5;
        CreateScheduleDTO dto = new TestCreateScheduleDTO().withYear(year).withMonth(month).build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);
        when(scheduleRepository.existsByStoreIdAndYearAndMonth(storeId, year, month)).thenReturn(true);

        //when
        EntityExistsException exception =
                assertThrows(EntityExistsException.class, () -> scheduleService.createSchedule(storeId, dto));

        //then
        assertEquals("Schedule for store id " + storeId + " in " + year + " and month " + month + " already exists",
                exception.getMessage());

        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(scheduleRepository, times(1)).existsByStoreIdAndYearAndMonth(storeId, year, month);
        verify(storeEntityService, never()).getEntityById(any());
        verify(scheduleBuilder, never()).createSchedule(any(), any(), any(), any(), any(),any());
    }

    @Test
    void updateSchedule_workingTest(){
        //given
        Long storeId = 1L;
        Long scheduleId = 10L;
        String updatedName = "Updated Name";
        Integer year = 2024;
        Integer month = 6;
        UpdateScheduleDTO dto = new TestUpdateScheduleDTO().withName(updatedName).withYear(year).withMonth(month).build();

        Schedule schedule = new TestScheduleBuilder().build();
        ResponseScheduleDTO responseDTO = new TestResponseScheduleDTO().withId(schedule.getId()).withName(updatedName).withYear(year).withMonth(month).build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(scheduleRepository.save(schedule)).thenReturn(schedule);
        when(scheduleMapper.toDTO(schedule)).thenReturn(responseDTO);

        //when
        ResponseScheduleDTO serviceResponse = scheduleService.updateSchedule(storeId, scheduleId, dto);

        //then
        assertNotNull(serviceResponse);
        assertEquals(responseDTO.id(), serviceResponse.id());

        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(scheduleRepository, times(1)).findById(scheduleId);
        verify(scheduleMapper, times(1)).updateSchedule(dto, schedule);
        verify(scheduleRepository, times(1)).save(schedule);
        verify(scheduleMapper, times(1)).toDTO(schedule);
    }

    @Test
    void updateSchedule_loggedUserHasNotAccessToThisStoreThrowsException(){
        //given
        Long storeId = 1L;
        Long scheduleId = 10L;
        UpdateScheduleDTO dto = new TestUpdateScheduleDTO().build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(false);

        //when
        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> scheduleService.updateSchedule(storeId, scheduleId, dto));

        //then
        assertEquals("Access denied for store with id " + storeId, exception.getMessage());

        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(scheduleRepository, never()).findById(any());
    }

    @Test
    void updateSchedule_scheduleByIdDoesNotExistThrowsException(){
        //given
        Long storeId = 1L;
        Long scheduleId = 999L;
        UpdateScheduleDTO dto = new TestUpdateScheduleDTO().build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.empty());

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> scheduleService.updateSchedule(storeId, scheduleId, dto));

        //then
        assertEquals("Cannot find schedule by id " + scheduleId, exception.getMessage());

        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(scheduleRepository, times(1)).findById(scheduleId);
        verify(scheduleMapper, never()).updateSchedule(any(), any());
    }

    @Test
    void findById_workingTest(){
        //given
        Long storeId = 1L;
        Long scheduleId = 10L;

        Schedule schedule = new TestScheduleBuilder().build();
        ResponseScheduleDTO responseDTO = new TestResponseScheduleDTO().withId(schedule.getId()).withName(schedule.getName()).withYear(schedule.getYear()).withMonth(schedule.getMonth()).build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(scheduleMapper.toDTO(schedule)).thenReturn(responseDTO);

        //when
        ResponseScheduleDTO serviceResponse = scheduleService.findById(storeId, scheduleId);

        //then
        assertNotNull(serviceResponse);
        assertEquals(responseDTO.id(), serviceResponse.id());
        assertEquals(responseDTO.name(), serviceResponse.name());

        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(scheduleRepository, times(1)).findById(scheduleId);
        verify(scheduleMapper, times(1)).toDTO(schedule);
    }

    @Test
    void findById_loggedUserHasNotAccessToThisStoreThrowsException(){
        //given
        Long storeId = 1L;
        Long scheduleId = 10L;

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(false);

        //when
        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> scheduleService.findById(storeId, scheduleId));

        //then
        assertEquals("Access denied for store with id " + storeId, exception.getMessage());

        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(scheduleRepository, never()).findById(any());
    }

    @Test
    void findById_scheduleByIdDoesNotExistThrowsException(){
        //given
        Long storeId = 1L;
        Long scheduleId = 999L;

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.empty());

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> scheduleService.findById(storeId, scheduleId));

        //then
        assertEquals("Cannot find schedule by id " + scheduleId, exception.getMessage());

        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(scheduleRepository, times(1)).findById(scheduleId);
        verify(scheduleMapper, never()).toDTO(any());
    }

    @Test
    void findByCriteria_workingTest(){
        //given
        Long storeId = 1L;
        ScheduleSpecificationDTO dto = new TestScheduleSpecificationDTO().withYear(2024).withMonth(5).build();
        Pageable pageable = PageRequest.of(0, 10);

        Schedule schedule1 = new TestScheduleBuilder().build();
        Schedule schedule2 = new TestScheduleBuilder().build();
        List<Schedule> schedules = Arrays.asList(schedule1, schedule2);
        Page<Schedule> schedulePage = new PageImpl<>(schedules, pageable, schedules.size());

        ResponseScheduleDTO responseDTO1 = new TestResponseScheduleDTO().withId(schedule1.getId()).withName(schedule1.getName()).withYear(schedule1.getYear()).withMonth(schedule1.getMonth()).build();
        ResponseScheduleDTO responseDTO2 = new TestResponseScheduleDTO().withId(schedule2.getId()).withName(schedule2.getName()).withYear(schedule2.getYear()).withMonth(schedule2.getMonth()).build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);
        when(scheduleRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(schedulePage);
        when(scheduleMapper.toDTO(schedule1)).thenReturn(responseDTO1);
        when(scheduleMapper.toDTO(schedule2)).thenReturn(responseDTO2);

        //when
        Page<ResponseScheduleDTO> serviceResponse = scheduleService.findByCriteria(storeId, dto, pageable);

        //then
        assertNotNull(serviceResponse);
        assertEquals(2, serviceResponse.getContent().size());

        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(scheduleRepository, times(1)).findAll(any(Specification.class), eq(pageable));
        verify(scheduleMapper, times(2)).toDTO(any(Schedule.class));
    }

    @Test
    void findByCriteria_loggedUserHasNotAccessToThisStoreThrowsException(){
        //given
        Long storeId = 1L;
        ScheduleSpecificationDTO dto = new TestScheduleSpecificationDTO().withYear(2024).withMonth(5).build();
        Pageable pageable = PageRequest.of(0, 10);

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(false);

        //when
        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> scheduleService.findByCriteria(storeId, dto, pageable));

        //then
        assertEquals("Access denied for store with id " + storeId, exception.getMessage());

        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(scheduleRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void findByCriteria_emptyResultsWorkingTest(){
        //given
        Long storeId = 1L;
        ScheduleSpecificationDTO dto = new ScheduleSpecificationDTO(
                null, 2024, 5, null, null, null, null, null, null
        );
        Pageable pageable = PageRequest.of(0, 10);

        Page<Schedule> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);
        when(scheduleRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        //when
        Page<ResponseScheduleDTO> serviceResponse = scheduleService.findByCriteria(storeId, dto, pageable);

        //then
        assertNotNull(serviceResponse);
        assertEquals(0, serviceResponse.getContent().size());

        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(scheduleRepository, times(1)).findAll(any(Specification.class), eq(pageable));
        verify(scheduleMapper, never()).toDTO(any(Schedule.class));
    }

    @Test
    void deleteSchedule_workingTest(){
        //given
        Long storeId = 1L;
        Long scheduleId = 10L;

        Schedule schedule = new TestScheduleBuilder().build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));

        //when
        scheduleService.deleteSchedule(storeId, scheduleId);

        //then
        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(scheduleRepository, times(1)).findById(scheduleId);
        verify(scheduleRepository, times(1)).delete(schedule);
    }

    @Test
    void deleteSchedule_loggedUserHasNotAccessToThisStoreThrowsException(){
        //given
        Long storeId = 1L;
        Long scheduleId = 10L;

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(false);

        //when
        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> scheduleService.deleteSchedule(storeId, scheduleId));

        //then
        assertEquals("Access denied for store with id " + storeId, exception.getMessage());

        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(scheduleRepository, never()).findById(any());
        verify(scheduleRepository, never()).delete(any(Schedule.class));
    }

    @Test
    void deleteSchedule_scheduleByIdDoesNotExistThrowsException(){
        //given
        Long storeId = 1L;
        Long scheduleId = 999L;

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.empty());

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> scheduleService.deleteSchedule(storeId, scheduleId));

        //then
        assertEquals("Cannot find schedule by id " + scheduleId, exception.getMessage());

        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(scheduleRepository, times(1)).findById(scheduleId);
        verify(scheduleRepository, never()).delete(any(Schedule.class));
    }

    @Test
    void saveSchedule_workingTest(){
        //given
        Schedule schedule = new TestScheduleBuilder().build();
        Long storeId = schedule.getStore().getId();

        ResponseScheduleDTO responseDTO = new TestResponseScheduleDTO().withId(schedule.getId()).withName(schedule.getName()).withYear(schedule.getYear()).withMonth(schedule.getMonth()).build();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);
        when(scheduleRepository.save(schedule)).thenReturn(schedule);
        when(scheduleMapper.toDTO(schedule)).thenReturn(responseDTO);

        //when
        ResponseScheduleDTO serviceResponse = scheduleService.saveSchedule(schedule);

        //then
        assertNotNull(serviceResponse);
        assertEquals(responseDTO.id(), serviceResponse.id());

        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(scheduleRepository, times(1)).save(schedule);
        verify(scheduleMapper, times(1)).toDTO(schedule);
    }

    @Test
    void saveSchedule_loggedUserHasNotAccessToThisStoreThrowsException(){
        //given
        Schedule schedule = new TestScheduleBuilder().build();
        Long storeId = schedule.getStore().getId();

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(false);

        //when
        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> scheduleService.saveSchedule(schedule));

        //then
        assertEquals("Access denied for store with id " + storeId, exception.getMessage());

        verify(userAuthorizationService, times(1)).hasAccessToStore(storeId);
        verify(scheduleRepository, never()).save(any());
        verify(scheduleMapper, never()).toDTO(any());
    }
}