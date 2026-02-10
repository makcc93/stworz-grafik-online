package online.stworzgrafik.StworzGrafik.schedule;

import online.stworzgrafik.StworzGrafik.schedule.DTO.CreateScheduleDTO;

public class TestCreateScheduleDTO {
    private Integer year = 2023;
    private Integer month = 5;
    private String name = "Majowy harmonogram 2023";
    private Long createdByUserId = 500L;
    private String scheduleStatusName = "done";

    public TestCreateScheduleDTO withYear(Integer year) {
        this.year = year;
        return this;
    }

    public TestCreateScheduleDTO withMonth(Integer month) {
        this.month = month;
        return this;
    }

    public TestCreateScheduleDTO withName(String name) {
        this.name = name;
        return this;
    }

    public TestCreateScheduleDTO withCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
        return this;
    }

    public TestCreateScheduleDTO withScheduleStatusName(String scheduleStatusName) {
        this.scheduleStatusName = scheduleStatusName;
        return this;
    }

    public CreateScheduleDTO build() {
        return new CreateScheduleDTO(
                year,
                month,
                name,
                createdByUserId,
                scheduleStatusName
        );
    }
}
