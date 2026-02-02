package online.stworzgrafik.StworzGrafik.schedule.details;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.CreateScheduleDetailsDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.ScheduleDetailsSpecificationDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.UpdateScheduleDetailsDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.ResponseScheduleDetailsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

@Validated
public interface ScheduleDetailsService {
    ResponseScheduleDetailsDTO createScheduleDetails(@NotNull Long scheduleId,
                                                     @NotNull @Valid CreateScheduleDetailsDTO dto);

    ResponseScheduleDetailsDTO updateScheduleDetails(@NotNull Long scheduleId,
                                                     @NotNull Long scheduleDetailsId,
                                                     @NotNull @Valid UpdateScheduleDetailsDTO dto);

    ResponseScheduleDetailsDTO findById(@NotNull Long scheduleId,
                                        @NotNull Long scheduleDetailsId);

    Page<ResponseScheduleDetailsDTO> findByCriteria(@NotNull Long scheduleId,
                                         ScheduleDetailsSpecificationDTO dto,
                                         Pageable pageable);

    void deleteScheduleDetails(@NotNull Long scheduleId,
                               @NotNull Long scheduleDetailsId);

    ResponseScheduleDetailsDTO saveScheduleDetails(@NotNull ScheduleDetails scheduleDetails);
}
