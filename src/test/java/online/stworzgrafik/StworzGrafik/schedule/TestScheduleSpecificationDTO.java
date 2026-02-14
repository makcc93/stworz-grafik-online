package online.stworzgrafik.StworzGrafik.schedule;

import online.stworzgrafik.StworzGrafik.schedule.DTO.ScheduleSpecificationDTO;

import java.time.LocalDateTime;

public class TestScheduleSpecificationDTO {
    private Long scheduleId = null;
    private Integer year = null;
    private Integer month = null;
    private String name = null;
    private LocalDateTime createdAt = null;
    private Long createdByUserId = null;
    private LocalDateTime updatedAt = null;
    private Long updatedByUserId = null;
    private String scheduleStatusName = null;

    public TestScheduleSpecificationDTO withScheduleId(Long scheduleId){
        this.scheduleId = scheduleId;
        return this;
    }

    public TestScheduleSpecificationDTO withYear(Integer year){
        this.year = year;
        return this;
    }

    public TestScheduleSpecificationDTO withMonth(Integer month){
        this.month = month;
        return this;
    }

    public TestScheduleSpecificationDTO withName(String name){
        this.name = name;
        return this;
    }

    public TestScheduleSpecificationDTO withCreatedAt(LocalDateTime createdAt){
        this.createdAt = createdAt;
        return this;
    }

    public TestScheduleSpecificationDTO withCreatedByUserId(Long createdByUserId){
        this.createdByUserId = createdByUserId;
        return this;
    }

    public TestScheduleSpecificationDTO withUpdatedAt(LocalDateTime updatedAt){
        this.updatedAt = updatedAt;
        return this;
    }

    public TestScheduleSpecificationDTO withUpdatedByUserId(Long updatedByUserId){
        this.updatedByUserId = updatedByUserId;
        return this;
    }

    public TestScheduleSpecificationDTO withScheduleStatusName(String scheduleStatusName){
        this.scheduleStatusName = scheduleStatusName;
        return this;
    }

    public ScheduleSpecificationDTO build(){
        return new ScheduleSpecificationDTO(
                scheduleId,
                year,
                month,
                name,
                createdAt,
                createdByUserId,
                updatedAt,
                updatedByUserId,
                scheduleStatusName
        );
    }
}
