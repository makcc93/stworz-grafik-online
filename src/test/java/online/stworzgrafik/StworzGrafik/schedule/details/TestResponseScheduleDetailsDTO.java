package online.stworzgrafik.StworzGrafik.schedule.details;

import online.stworzgrafik.StworzGrafik.schedule.details.DTO.ResponseScheduleDetailsDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TestResponseScheduleDetailsDTO {
    private Long id = 12L;
    private Long scheduleId = 13L;
    private Long employeeId = 14L;
    private LocalDate date = LocalDate.of(2022,12,10);
    private Long shiftId = 15L;
    private Long shiftTypeConfigId = 16L;
    private LocalDateTime createdAt = LocalDateTime.of(2022,1,1,12,30);
    private LocalDateTime updatedAt = null;

    public TestResponseScheduleDetailsDTO withId(Long id){
        this.id = id;
        return this;
    }

    public TestResponseScheduleDetailsDTO withScheduleId(Long scheduleId){
        this.scheduleId = scheduleId;
        return this;
    }

    public TestResponseScheduleDetailsDTO withEmployeeId(Long employeeId){
        this.employeeId = employeeId;
        return this;
    }

    public TestResponseScheduleDetailsDTO withDate(LocalDate date){
        this.date = date;
        return this;
    }

    public TestResponseScheduleDetailsDTO withShiftId(Long shiftId){
        this.shiftId = shiftId;
        return this;
    }

    public TestResponseScheduleDetailsDTO withShiftTypeConfigId(Long shiftTypeConfigId){
        this.shiftTypeConfigId = shiftTypeConfigId;
        return this;
    }

    public TestResponseScheduleDetailsDTO withCreatedAt(LocalDateTime createdAt){
        this.createdAt = createdAt;
        return this;
    }

    public TestResponseScheduleDetailsDTO withUpdatedAt(LocalDateTime updatedAt){
        this.updatedAt = updatedAt;
        return this;
    }

    public ResponseScheduleDetailsDTO build(){
        return new ResponseScheduleDetailsDTO(
                id,
                scheduleId,
                employeeId,
                date,
                shiftId,
                shiftTypeConfigId,
                createdAt,
                updatedAt
        );
    }
}
