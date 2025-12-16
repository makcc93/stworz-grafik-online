package online.stworzgrafik.StworzGrafik.draft;

import online.stworzgrafik.StworzGrafik.draft.DTO.CreateDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.draft.DTO.UpdateDemandDraftDTO;

import java.time.LocalDate;

public class TestUpdateDemandDraftDTO {
    private LocalDate draftDate = LocalDate.of(2023,12,6);
    private int[] hourlyDemand = {0,0,0,0,0,0,0,0,2,4,7,7,7,7,9,9,9,9,9,9,8,0,0,0};

    public TestUpdateDemandDraftDTO withDraftDate(LocalDate draftDate){
        this.draftDate = draftDate;
        return this;
    }

    public TestUpdateDemandDraftDTO withHourlyDemand(int[] hourlyDemand){
        this.hourlyDemand = hourlyDemand;
        return this;
    }

    public UpdateDemandDraftDTO build(){
        return new UpdateDemandDraftDTO(
                draftDate,
                hourlyDemand
        );
    }
}

