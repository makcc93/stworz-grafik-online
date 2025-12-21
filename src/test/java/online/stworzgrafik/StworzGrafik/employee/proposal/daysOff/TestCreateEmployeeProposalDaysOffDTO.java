package online.stworzgrafik.StworzGrafik.employee.proposal.daysOff;

import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.CreateEmployeeProposalDaysOffDTO;

public class TestCreateEmployeeProposalDaysOffDTO {
    private Integer year = 2010;
    private Integer month = 10;
    private int[] monthlyDaysOff = {1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

    public TestCreateEmployeeProposalDaysOffDTO withYear(Integer year){
        this.year = year;
        return this;
    }

    public TestCreateEmployeeProposalDaysOffDTO withMonth(Integer month){
        this.month = month;
        return this;
    }

    public TestCreateEmployeeProposalDaysOffDTO withMonthlyDaysOff(int[] monthlyDaysOff){
        this.monthlyDaysOff = monthlyDaysOff;
        return this;
    }
    public CreateEmployeeProposalDaysOffDTO build(){
        return new CreateEmployeeProposalDaysOffDTO(
                year,
                month,
                monthlyDaysOff
        );
    }
}
