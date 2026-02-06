package online.stworzgrafik.StworzGrafik.schedule;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.schedule.DTO.CreateScheduleDTO;
import online.stworzgrafik.StworzGrafik.schedule.DTO.ResponseScheduleDTO;
import online.stworzgrafik.StworzGrafik.schedule.DTO.ScheduleSpecificationDTO;
import online.stworzgrafik.StworzGrafik.schedule.DTO.UpdateScheduleDTO;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.data.domain.Pageable;

@Validated
public interface ScheduleService {
    ResponseScheduleDTO createSchedule(@NotNull Long storeId,
                                       @NotNull @Valid CreateScheduleDTO dto);

    ResponseScheduleDTO updateSchedule(@NotNull Long storeId,
                                       @NotNull Long scheduleId,
                                       @NotNull @Valid UpdateScheduleDTO dto);

    ResponseScheduleDTO findById(@NotNull Long storeId,
                                 @NotNull Long scheduleId);

    Page<ResponseScheduleDTO> findByCriteria(ScheduleSpecificationDTO dto,
                                             Pageable pageable);

    void deleteSchedule(@NotNull Long storeId,
                        @NotNull Long scheduleId);

    ResponseScheduleDTO saveSchedule(@NotNull Schedule schedule);
}

