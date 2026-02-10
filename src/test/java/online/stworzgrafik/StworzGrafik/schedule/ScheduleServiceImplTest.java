package online.stworzgrafik.StworzGrafik.schedule;

import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
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
    void findByEntity_loggedUserHasNotAccessToThisStoreThrowsException(){
        //given
        Long scheduleId = 54321L;
        Long storeId = 1337L;

        Schedule schedule = new TestScheduleBuilder().build();
        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.ofNullable(schedule));

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(false);

        //when
        AccessDeniedException exception =
        assertThrows(AccessDeniedException.class, () -> scheduleService.findEntityById(scheduleId));

        //then
        assertEquals("Access denied for store with id " + storeId, exception.getMessage());

        verify(scheduleRepository,times(1)).findById(any());
        verify(userAuthorizationService,times(1)).hasAccessToStore(storeId);
    }
}