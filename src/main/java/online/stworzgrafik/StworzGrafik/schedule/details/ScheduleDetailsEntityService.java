package online.stworzgrafik.StworzGrafik.schedule.details;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.ScheduleDetailsSpecificationDTO;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.UpdateScheduleDetailsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;

@Validated
public interface ScheduleDetailsEntityService {
    Page<ScheduleDetails> findEntityByCriteria(@NotNull Long storeId,
                                               @NotNull Long scheduleId,
                                               ScheduleDetailsSpecificationDTO dto,
                                               Pageable pageable);

    ScheduleDetails findEmployeeShiftByDay(@NotNull Long storeId,
                                           @NotNull Long scheduleId,
                                           @NotNull Employee employee,
                                           @NotNull LocalDate day);

    ScheduleDetails updateEntityScheduleDetails(@NotNull Long storeId,
                                                @NotNull Long scheduleId,
                                                @NotNull Long scheduleDetailsId,
                                                @NotNull @Valid UpdateScheduleDetailsDTO dto);
}
