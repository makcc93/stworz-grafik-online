package online.stworzgrafik.StworzGrafik.employee.proposal.shifts;

import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO.CreateEmployeeProposalShiftsDTO;

import java.time.LocalDate;

public class TestCreateEmployeeProposalShiftsDTO {
    private LocalDate date = LocalDate.of(2020,05,10);
    private int[] dailyProposalShift = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0};

    public TestCreateEmployeeProposalShiftsDTO withDate(LocalDate date){
        this.date = date;
        return this;
    }

    public TestCreateEmployeeProposalShiftsDTO withDailyProposalShift(int[] dailyProposalShift){
        this.dailyProposalShift = dailyProposalShift;
        return this;
    }

    public CreateEmployeeProposalShiftsDTO build(){
        return new CreateEmployeeProposalShiftsDTO(
                date,
                dailyProposalShift
        );
    }
}
