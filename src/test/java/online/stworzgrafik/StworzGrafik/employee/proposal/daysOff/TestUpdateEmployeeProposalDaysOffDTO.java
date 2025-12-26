package online.stworzgrafik.StworzGrafik.employee.proposal.daysOff;

import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.UpdateEmployeeProposalDaysOffDTO;

import java.time.LocalDateTime;

public class TestUpdateEmployeeProposalDaysOffDTO {
    private Long storeId = 5L;
    private Long employeeId = 3L;
    private Integer year = 2021;
    private Integer month = 11;
    private int[] monthlyDaysOff = {1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    private LocalDateTime updatedAt = LocalDateTime.of(2024,11,20,8,0);

    public TestUpdateEmployeeProposalDaysOffDTO withStoreId(Long storeId){
        this.storeId = storeId;
        return this;
    }

    public TestUpdateEmployeeProposalDaysOffDTO withEmployeeId(Long employeeId){
        this.employeeId = employeeId;
        return this;
    }

    public TestUpdateEmployeeProposalDaysOffDTO withYear(Integer year){
        this.year = year;
        return this;
    }

    public TestUpdateEmployeeProposalDaysOffDTO withMonth(Integer month){
        this.month = month;
        return this;
    }

    public TestUpdateEmployeeProposalDaysOffDTO withMonthlyDaysOff(int[] monthlyDaysOff) {
        this.monthlyDaysOff = monthlyDaysOff;
        return this;
    }

    public TestUpdateEmployeeProposalDaysOffDTO withUpdatedAt(LocalDateTime updatedAt){
        this.updatedAt = updatedAt;
        return this;
    }

    public UpdateEmployeeProposalDaysOffDTO build(){
        return new UpdateEmployeeProposalDaysOffDTO(
                storeId,
                employeeId,
                year,
                month,
                monthlyDaysOff,
                updatedAt
        );
    }
}
