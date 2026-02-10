package online.stworzgrafik.StworzGrafik.schedule;

import online.stworzgrafik.StworzGrafik.schedule.DTO.ResponseScheduleDTO;

import java.time.LocalDateTime;

public class TestResponseScheduleDTO {
    private Long id = 1000L;
    private Long storeId = 2000L;
    private Integer year = 2023;
    private Integer month = 5;
    private String name = "Harmonogram Kwiecień 2023";
    private LocalDateTime createdAt = LocalDateTime.of(2023, 4, 25, 10, 30);
    private Long createdByUserId = 501L;
    private LocalDateTime updatedAt = LocalDateTime.of(2023, 4, 28, 14, 45);
    private Long updatedByUserId = 502L;
    private String scheduleStatusName = "DONE";

    public TestResponseScheduleDTO withId(Long id) {
        this.id = id;
        return this;
    }

    public TestResponseScheduleDTO withStoreId(Long storeId) {
        this.storeId = storeId;
        return this;
    }

    public TestResponseScheduleDTO withYear(Integer year) {
        this.year = year;
        return this;
    }

    public TestResponseScheduleDTO withMonth(Integer month) {
        this.month = month;
        return this;
    }

    public TestResponseScheduleDTO withName(String name) {
        this.name = name;
        return this;
    }

    public TestResponseScheduleDTO withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public TestResponseScheduleDTO withCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
        return this;
    }

    public TestResponseScheduleDTO withUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public TestResponseScheduleDTO withUpdatedByUserId(Long updatedByUserId) {
        this.updatedByUserId = updatedByUserId;
        return this;
    }

    public TestResponseScheduleDTO withScheduleStatusName(String scheduleStatusName) {
        this.scheduleStatusName = scheduleStatusName;
        return this;
    }

    public ResponseScheduleDTO build() {
        return new ResponseScheduleDTO(
                id,
                storeId,
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
