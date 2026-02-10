package online.stworzgrafik.StworzGrafik.schedule.details;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.UpdateScheduleDetailsDTO;

import java.time.LocalDate;

public class TestUpdateScheduleDetailsDTO {
    private Long employeeId = 103L;
    private LocalDate date = LocalDate.of(2023, 5, 17);
    private Long shiftId = 203L;
    private Long shiftTypeConfigId = 303L;

    public TestUpdateScheduleDetailsDTO withEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
        return this;
    }

    public TestUpdateScheduleDetailsDTO withDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public TestUpdateScheduleDetailsDTO withShiftId(Long shiftId) {
        this.shiftId = shiftId;
        return this;
    }

    public TestUpdateScheduleDetailsDTO withShiftTypeConfigId(Long shiftTypeConfigId) {
        this.shiftTypeConfigId = shiftTypeConfigId;
        return this;
    }

    public UpdateScheduleDetailsDTO build() {
        return new UpdateScheduleDetailsDTO(
                employeeId,
                date,
                shiftId,
                shiftTypeConfigId
        );
    }
}
