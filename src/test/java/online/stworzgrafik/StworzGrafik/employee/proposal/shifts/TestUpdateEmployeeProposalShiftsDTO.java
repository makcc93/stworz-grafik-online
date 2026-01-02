package online.stworzgrafik.StworzGrafik.employee.proposal.shifts;

import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO.UpdateEmployeeProposalShiftsDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TestUpdateEmployeeProposalShiftsDTO {
    private LocalDate date = LocalDate.of(2020,05,10);
    private int[] dailyProposalShift = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0};
    private LocalDateTime updatedAt = LocalDateTime.now();

    public TestUpdateEmployeeProposalShiftsDTO withDate(LocalDate date){
        this.date = date;
        return this;
    }

    public TestUpdateEmployeeProposalShiftsDTO withDailyProposalShift(int[] dailyProposalShift){
        this.dailyProposalShift = dailyProposalShift;
        return this;
    }

    public TestUpdateEmployeeProposalShiftsDTO withUpdatedAt(LocalDateTime updatedAt){
        this.updatedAt = updatedAt;
        return this;
    }

    public UpdateEmployeeProposalShiftsDTO build(){
        return new UpdateEmployeeProposalShiftsDTO(
                date,
                dailyProposalShift,
                updatedAt
        );
    }
}
