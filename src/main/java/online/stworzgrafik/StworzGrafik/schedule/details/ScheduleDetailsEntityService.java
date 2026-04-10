package online.stworzgrafik.StworzGrafik.schedule.details;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.CreateScheduleDetailsDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.ScheduleDetailsSpecificationDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.UpdateScheduleDetailsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Validated
public interface ScheduleDetailsEntityService {
    ScheduleDetails add(@NotNull Long storeId, @NotNull Long scheduleId, CreateScheduleDetailsDTO dto);

    Page<ScheduleDetails> findEntityByCriteria(@NotNull Long storeId,
                                               @NotNull Long scheduleId,
                                               ScheduleDetailsSpecificationDTO dto,
                                               Pageable pageable);

    Optional<ScheduleDetails> findEmployeeScheduleDetailsByDay(@NotNull Long storeId,
                                                               @NotNull Long scheduleId,
                                                               @NotNull Employee employee,
                                                               @NotNull LocalDate day);

    ScheduleDetails updateEntityScheduleDetails(@NotNull Long storeId,
                                                @NotNull Long scheduleId,
                                                @NotNull Long scheduleDetailsId,
                                                @NotNull @Valid UpdateScheduleDetailsDTO dto);

    List<ScheduleDetails> findDailyScheduleDetails(@NotNull Long storeId,
                                                   @NotNull Long scheduleId,
                                                   @NotNull LocalDate date);
}
