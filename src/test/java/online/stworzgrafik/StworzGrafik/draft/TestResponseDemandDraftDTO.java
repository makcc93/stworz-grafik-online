package online.stworzgrafik.StworzGrafik.draft;

import online.stworzgrafik.StworzGrafik.draft.DTO.ResponseDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

public class TestResponseDemandDraftDTO {
    private Long id = 1L;
    private Store store = new TestStoreBuilder().build();
    private Integer year = 2025;
    private Integer month = 1;
    private Integer day = 1;
    private int[] hourlyDemand = {0,0,0,0,0,0,0,0,4,8,9,9,9,9,11,11,11,11,11,11,7,0,0,0};
    private LocalDateTime createdAt = LocalDateTime.of(2000,12,12,20,0);
    private LocalDateTime updatedAt = null;

    public TestResponseDemandDraftDTO withId(Long id){
        this.id = id;
        return this;
    }

    public TestResponseDemandDraftDTO withStore(Store store){
        this.store = store;
        return this;
    }

    public TestResponseDemandDraftDTO withYear(Integer year){
        this.year = year;
        return this;
    }

    public TestResponseDemandDraftDTO withMonth(Integer month){
        this.month = month;
        return this;
    }

    public TestResponseDemandDraftDTO withDay(Integer day){
        this.day = day;
        return this;
    }

    public TestResponseDemandDraftDTO withHourlyDemand(int[] hourlyDemand){
        this.hourlyDemand = hourlyDemand;
        return this;
    }

    public TestResponseDemandDraftDTO withCreatedAt(LocalDateTime createdAt){
        this.createdAt = createdAt;
        return this;
    }

    public TestResponseDemandDraftDTO withUpdatedAt(LocalDateTime updatedAt){
        this.updatedAt = updatedAt;
        return this;
    }

    public ResponseDemandDraftDTO build(){
        return new ResponseDemandDraftDTO(
                id,
                store,
                year,
                month,
                day,
                hourlyDemand,
                createdAt,
                updatedAt

        );
    }
}
