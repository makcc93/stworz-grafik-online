package online.stworzgrafik.StworzGrafik.schedule.details;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.schedule.Schedule;
import online.stworzgrafik.StworzGrafik.schedule.ScheduleEntityService;
import online.stworzgrafik.StworzGrafik.schedule.ScheduleService;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.CreateScheduleDetailsDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.ResponseScheduleDetailsDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.ScheduleDetailsSpecificationDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.UpdateScheduleDetailsDTO;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.ShiftEntityService;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftTypeConfig;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftTypeConfigService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import static online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetailsSpecification.*;

@Service
@RequiredArgsConstructor
public class ScheduleDetailsServiceImpl implements ScheduleDetailsService{
    private final ScheduleDetailsRepository repository;
    private final UserAuthorizationService userAuthorizationService;
    private final ScheduleService scheduleService;
    private final ScheduleEntityService scheduleEntityService;
    private final EmployeeEntityService employeeService;
    private final ShiftEntityService shiftService;
    private final ShiftTypeConfigService shiftTypeConfigService;
    private final ScheduleDetailsBuilder builder;
    private final ScheduleDetailsMapper mapper;

    @Override
    public ResponseScheduleDetailsDTO createScheduleDetails(Long storeId, Long scheduleId, CreateScheduleDetailsDTO dto) {
        verifyUserToStoreAccess(storeId);

        Schedule schedule = scheduleEntityService.findEntityById(scheduleId);


        if (repository.existsByEmployeeIdAndDate(dto.employeeId(),dto.date())){
            throw new EntityExistsException("Schedule details for employee id " + dto.employeeId()
                    + " on date " + dto.date()
                    + " already exists");
        }

        verifyScheduleAndStoreMatching(storeId, scheduleId, schedule);

        Employee employee = employeeService.getEntityById(storeId);
        Shift shift = shiftService.getEntityById(dto.shiftId());
        ShiftTypeConfig shiftTypeConfig = shiftTypeConfigService.findById(dto.shiftTypeConfigId());

        ScheduleDetails scheduleDetails = builder.createScheduleDetails(
                schedule,
                employee,
                dto.date(),
                shift,
                shiftTypeConfig
        );

        ScheduleDetails saved = repository.save(scheduleDetails);

        return mapper.toDTO(saved);
    }

    @Override
    public ResponseScheduleDetailsDTO updateScheduleDetails(Long storeId, Long scheduleId, Long scheduleDetailsId, UpdateScheduleDetailsDTO dto) {
        verifyUserToStoreAccess(storeId);

        if (!repository.existsByEmployeeIdAndDate(dto.employeeId(),dto.date())){
            throw new EntityNotFoundException("Schedule details for employee id " + dto.employeeId()
                    + " on date " + dto.date()
                    + " does not exist");
        }

        ScheduleDetails scheduleDetails = getScheduleDetails(scheduleDetailsId);

        mapper.updateScheduleDetails(dto,scheduleDetails);

        ScheduleDetails saved = repository.save(scheduleDetails);

        return mapper.toDTO(saved);
    }

    @Override
    public ResponseScheduleDetailsDTO findById(Long storeId, Long scheduleId, Long scheduleDetailsId) {
        verifyUserAccessAndData(storeId, scheduleId);

        ScheduleDetails scheduleDetails = getScheduleDetails(scheduleDetailsId);

        return mapper.toDTO(scheduleDetails);
    }

    @Override
    public Page<ResponseScheduleDetailsDTO> findByCriteria(Long storeId, Long scheduleId, ScheduleDetailsSpecificationDTO dto, Pageable pageable) {
        verifyUserAccessAndData(storeId, scheduleId);

        Specification<ScheduleDetails> specification  = Specification.allOf(
                hasScheduleId(scheduleId),
                hasId(dto.scheduleDetailsId()),
                hasEmployeeId(dto.employeeId()),
                hasDate(dto.date()),
                hasShift(dto.shiftId()),
                hasShiftTypeConfig(dto.shiftTypeConfigId())
        );

        return repository.findAll(specification, pageable)
                .map(mapper::toDTO);
    }

    @Override
    public void deleteScheduleDetails(Long storeId, Long scheduleId, Long scheduleDetailsId) {
        verifyUserAccessAndData(storeId, scheduleId);

        repository.deleteById(scheduleDetailsId);
    }

    @Override
    public ResponseScheduleDetailsDTO saveScheduleDetails(ScheduleDetails scheduleDetails) {
        return mapper.toDTO(repository.save(scheduleDetails));
    }

    private void verifyUserAccessAndData(Long storeId, Long scheduleId) {
        verifyUserToStoreAccess(storeId);

        Schedule schedule = scheduleEntityService.findEntityById(scheduleId);
        verifyScheduleAndStoreMatching(storeId, scheduleId, schedule);
    }

    private void verifyUserToStoreAccess(Long storeId) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }
    }

    private ScheduleDetails getScheduleDetails(Long scheduleDetailsId) {
        return repository.findById(scheduleDetailsId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find schedule details by id " + scheduleDetailsId));
    }

    private static void verifyScheduleAndStoreMatching(Long storeId, Long scheduleId, Schedule schedule) {
        if (!schedule.getStore().getId().equals(storeId)){
            throw new AccessDeniedException("Schedule id " + scheduleId + " does not belong to store with id " + storeId);
        }
    }
}
