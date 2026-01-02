package online.stworzgrafik.StworzGrafik.employee.proposal.shifts;

import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO.ResponseEmployeeProposalShiftsDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TestResponseEmployeeProposalShiftsDTO {
    private Long id = 100L;
    private Long storeId = 200L;
    private Long employeeId = 300L;
    private LocalDate date = LocalDate.of(2020,05,10);
    private int[] dailyProposalShift = {0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0};
    private LocalDateTime createdAt = LocalDateTime.of(2022,12,12,12,12);
    private LocalDateTime updatedAt = LocalDateTime.now();

    public TestResponseEmployeeProposalShiftsDTO withId(Long id){
        this.id = id;
        return this;
    }

    public TestResponseEmployeeProposalShiftsDTO withStoreId(Long storeId){
        this.storeId = storeId;
        return this;
    }

    public TestResponseEmployeeProposalShiftsDTO withEmployeeId(Long employeeId){
        this.employeeId = employeeId;
        return this;
    }

    public TestResponseEmployeeProposalShiftsDTO withDate(LocalDate date){
        this.date = date;
        return this;
    }

    public TestResponseEmployeeProposalShiftsDTO withDailyProposalShift(int[] dailyProposalShift){
        this.dailyProposalShift = dailyProposalShift;
        return this;
    }

    public TestResponseEmployeeProposalShiftsDTO withCreatedAt(LocalDateTime createdAt){
        this.createdAt = createdAt;
        return this;
    }

    public TestResponseEmployeeProposalShiftsDTO withUpdatedAt(LocalDateTime updatedAt){
        this.updatedAt = updatedAt;
        return this;
    }

    public ResponseEmployeeProposalShiftsDTO build(){
        return new ResponseEmployeeProposalShiftsDTO(
                id,
                storeId,
                employeeId,
                date,
                dailyProposalShift,
                createdAt,
                updatedAt
        );
    }
}
