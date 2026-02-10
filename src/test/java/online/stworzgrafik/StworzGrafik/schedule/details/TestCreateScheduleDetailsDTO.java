package online.stworzgrafik.StworzGrafik.schedule.details;
import online.stworzgrafik.StworzGrafik.schedule.details.DTO.CreateScheduleDetailsDTO;

import java.time.LocalDate;

public class TestCreateScheduleDetailsDTO {
    private Long employeeId = 101L;
    private LocalDate date = LocalDate.of(2023, 5, 15);
    private Long shiftId = 201L;
    private Long shiftTypeConfigId = 301L;

    public TestCreateScheduleDetailsDTO withEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
        return this;
    }

    public TestCreateScheduleDetailsDTO withDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public TestCreateScheduleDetailsDTO withShiftId(Long shiftId) {
        this.shiftId = shiftId;
        return this;
    }

    public TestCreateScheduleDetailsDTO withShiftTypeConfigId(Long shiftTypeConfigId) {
        this.shiftTypeConfigId = shiftTypeConfigId;
        return this;
    }

    public CreateScheduleDetailsDTO build() {
        return new CreateScheduleDetailsDTO(
                employeeId,
                date,
                shiftId,
                shiftTypeConfigId
        );
    }
}
