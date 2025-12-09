package online.stworzgrafik.StworzGrafik.draft;

import online.stworzgrafik.StworzGrafik.draft.DTO.CreateDemandDraftDTO;

public class TestCreateDemandDraftDTO {
    private Integer year = 2025;
    private Integer month = 1;
    private Integer day = 1;
    private int[] hourlyDemand = {0,0,0,0,0,0,0,0,2,4,6,6,6,6,8,8,8,8,8,8,6,0,0,0};

    public TestCreateDemandDraftDTO withYear(Integer year){
        this.year = year;
        return this;
    }

    public TestCreateDemandDraftDTO withMonth(Integer month){
        this.month = month;
        return this;
    }

    public TestCreateDemandDraftDTO withDay(Integer day){
        this.day = day;
        return this;
    }

    public TestCreateDemandDraftDTO withHourlyDemand(int[] hourlyDemand){
        this.hourlyDemand = hourlyDemand;
        return this;
    }

    public CreateDemandDraftDTO build(){
        return new CreateDemandDraftDTO(
                year,
                month,
                day,
                hourlyDemand
        );
    }

}
