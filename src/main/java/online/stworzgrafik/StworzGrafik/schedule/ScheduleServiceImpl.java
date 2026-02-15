package online.stworzgrafik.StworzGrafik.schedule;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.schedule.DTO.CreateScheduleDTO;
import online.stworzgrafik.StworzGrafik.schedule.DTO.ResponseScheduleDTO;
import online.stworzgrafik.StworzGrafik.schedule.DTO.ScheduleSpecificationDTO;
import online.stworzgrafik.StworzGrafik.schedule.DTO.UpdateScheduleDTO;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static online.stworzgrafik.StworzGrafik.schedule.ScheduleSpecification.*;

@Service
@RequiredArgsConstructor
class ScheduleServiceImpl implements ScheduleService, ScheduleEntityService{
    private final ScheduleRepository repository;
    private final ScheduleBuilder builder;
    private final ScheduleMapper mapper;
    private final UserAuthorizationService userAuthorizationService;
    private final StoreEntityService storeEntityService;

    @Override
    public Schedule findEntityById(Long scheduleId) {
        Schedule schedule = getSchedule(scheduleId);

        Long storeId = schedule.getStore().getId();
        verifyStoreAccess(storeId);

        return schedule;
    }

    @Override
    public ResponseScheduleDTO createSchedule(Long storeId, CreateScheduleDTO dto) {
        verifyStoreAccess(storeId);

        if (repository.existsByStoreIdAndYearAndMonth(storeId,dto.year(),dto.month())){
            throw new EntityExistsException("Schedule for store id " + storeId
                    + " in " + dto.year()
                    + " and month " + dto.month()
                    + " already exists");
        }

        Store store = storeEntityService.getEntityById(storeId);
        ScheduleStatus scheduleStatus = mapper.stringToEnum(dto.scheduleStatusName());

        Schedule schedule = builder.createSchedule(
                store,
                dto.year(),
                dto.month(),
                dto.name(),
                scheduleStatus,
                dto.createdByUserId()
        );

        Schedule saved = repository.save(schedule);

        return mapper.toDTO(saved);
    }

    @Override
    public ResponseScheduleDTO updateSchedule(Long storeId, Long scheduleId, UpdateScheduleDTO dto) {
        verifyStoreAccess(storeId);

        Schedule schedule = getSchedule(scheduleId);

        mapper.updateSchedule(dto,schedule);

        Schedule saved = repository.save(schedule);

        return mapper.toDTO(saved);
    }

    @Override
    public ResponseScheduleDTO findById(Long storeId, Long scheduleId) {
        verifyStoreAccess(storeId);

        Schedule schedule = getSchedule(scheduleId);

        return mapper.toDTO(schedule);
    }

    @Override
    public Page<ResponseScheduleDTO> findByCriteria(Long storeId, ScheduleSpecificationDTO dto, Pageable pageable) {
        verifyStoreAccess(storeId);

        Specification<Schedule> specification = Specification.allOf(
                hasId(dto.scheduleId()),
                hasYear(dto.year()),
                hasMonth(dto.month()),
                hasName(dto.name()),
                isCreatedAt(dto.createdAt()),
                isCreatedBy(dto.createdByUserId()),
                isUpdatedAt(dto.updatedAt()),
                isUpdatedBy(dto.updatedByUserId()),
                hasScheduleStatusName(dto.scheduleStatusName())
        );

        return repository.findAll(specification,pageable)
                .map(mapper::toDTO);
    }

    @Override
    public void deleteSchedule(Long storeId, Long scheduleId) {
        verifyStoreAccess(storeId);

        Schedule schedule = getSchedule(scheduleId);

        repository.delete(schedule);
    }

    @Override
    public ResponseScheduleDTO saveSchedule(Schedule schedule) {
        Long storeId = schedule.getStore().getId();
        verifyStoreAccess(storeId);

        return mapper.toDTO(repository.save(schedule));
    }

    private void verifyStoreAccess(Long storeId) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }
    }

    private Schedule getSchedule(Long scheduleId) {
        return repository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find schedule by id " + scheduleId));
    }
}
