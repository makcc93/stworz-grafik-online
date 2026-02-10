package online.stworzgrafik.StworzGrafik.schedule;
import online.stworzgrafik.StworzGrafik.schedule.DTO.UpdateScheduleDTO;

import java.time.LocalDateTime;

public class TestUpdateScheduleDTO {
    private Long storeId = 2001L;
    private Integer year = 2023;
    private Integer month = 7;
    private String name = "Zaktualizowany harmonogram";
    private LocalDateTime updatedAt = LocalDateTime.of(2023, 6, 15, 11, 0);
    private Long updatedByUserId = 505L;

    public TestUpdateScheduleDTO withStoreId(Long storeId) {
        this.storeId = storeId;
        return this;
    }

    public TestUpdateScheduleDTO withYear(Integer year) {
        this.year = year;
        return this;
    }

    public TestUpdateScheduleDTO withMonth(Integer month) {
        this.month = month;
        return this;
    }

    public TestUpdateScheduleDTO withName(String name) {
        this.name = name;
        return this;
    }

    public TestUpdateScheduleDTO withUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public TestUpdateScheduleDTO withUpdatedByUserId(Long updatedByUserId) {
        this.updatedByUserId = updatedByUserId;
        return this;
    }

    public UpdateScheduleDTO build() {
        return new UpdateScheduleDTO(
                storeId,
                year,
                month,
                name,
                updatedAt,
                updatedByUserId
        );
    }
}
