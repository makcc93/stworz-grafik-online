package online.stworzgrafik.StworzGrafik.schedule.message;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.schedule.Schedule;
import online.stworzgrafik.StworzGrafik.schedule.ScheduleEntityService;
import online.stworzgrafik.StworzGrafik.schedule.message.DTO.CreateScheduleMessageDTO;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
class ScheduleMessageServiceImpl implements ScheduleMessageService {
    private final ScheduleMessageRepository scheduleMessageRepository;
    private final UserAuthorizationService userAuthorizationService;
    private final ScheduleEntityService scheduleEntityService;
    private final ScheduleMessageBuilder scheduleMessageBuilder;
    private final EmployeeEntityService employeeEntityService;

    @Override
    public void save(Long scheduleId, CreateScheduleMessageDTO dto) {
        Schedule schedule = scheduleEntityService.findEntityById(scheduleId);

        if (!userAuthorizationService.getUserStoreId().equals(schedule.getStore().getId())){
            throw new AccessDeniedException("Logged user do not have access to schedule with id " + scheduleId);
        }

        Employee employee = null;
        if (dto.employeeId() != null) {
            employee = employeeEntityService.getEntityById(dto.employeeId());
        }
        ScheduleMessage scheduleMessage = scheduleMessageBuilder.create(
                schedule,
                dto.scheduleMessageType(),
                dto.scheduleMessageCode(),
                dto.message(),
                employee,
                dto.messageDate()
        );

        scheduleMessageRepository.save(scheduleMessage);
    }

    @Override
    public void delete(Long scheduleId, Long scheduleMessageId) {
        Schedule schedule = scheduleEntityService.findEntityById(scheduleId);

        if (!userAuthorizationService.getUserStoreId().equals(schedule.getStore().getId())){
            throw new AccessDeniedException("Logged user do not have access to schedule with id " + scheduleId);
        }

        ScheduleMessage scheduleMessage = scheduleMessageRepository.findById(scheduleMessageId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find Schedule Message by id " + scheduleMessageId));

        scheduleMessageRepository.delete(scheduleMessage);
    }
}
