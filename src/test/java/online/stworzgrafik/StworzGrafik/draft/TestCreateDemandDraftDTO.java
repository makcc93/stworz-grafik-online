package online.stworzgrafik.StworzGrafik.draft;

import online.stworzgrafik.StworzGrafik.draft.DTO.CreateDemandDraftDTO;

import java.time.LocalDate;

public class TestCreateDemandDraftDTO {
    private LocalDate draftDate = LocalDate.of(2020,10,5);
    private int[] hourlyDemand = {0,0,0,0,0,0,0,0,2,4,6,6,6,6,8,8,8,8,8,8,6,0,0,0};

    public TestCreateDemandDraftDTO withDraftDate(LocalDate draftDate){
        this.draftDate = draftDate;
        return this;
    }

    public TestCreateDemandDraftDTO withHourlyDemand(int[] hourlyDemand){
        this.hourlyDemand = hourlyDemand;
        return this;
    }

    public CreateDemandDraftDTO build(){
        return new CreateDemandDraftDTO(
                draftDate,
                hourlyDemand
        );
    }
}
