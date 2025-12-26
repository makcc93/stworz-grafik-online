package online.stworzgrafik.StworzGrafik.employee.proposal.daysOff;

import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.ResponseEmployeeProposalDaysOffDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TestResponseEmployeeProposalDaysOffDTO {

    private Long id = 1L;
    private Long storeId = 123L;
    private Long employeeId = 321L;
    private Integer year = 2022;
    private Integer month = 10;
    private int[] monthlyDaysOff = {1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    private LocalDateTime createdAt = LocalDateTime.of(2010,10,10,10,10);
    private LocalDateTime updatedAt = LocalDateTime.of(2020,10,20,20,20);

    public TestResponseEmployeeProposalDaysOffDTO withId(Long id){
        this.id = id;
        return this;
    }

    public TestResponseEmployeeProposalDaysOffDTO withStoreId(Long storeId){
        this.storeId = storeId;
        return this;
    }

    public TestResponseEmployeeProposalDaysOffDTO withEmployeeId(Long employeeId){
        this.employeeId = employeeId;
        return this;
    }

    public TestResponseEmployeeProposalDaysOffDTO withYear(Integer year){
        this.year = year;
        return this;
    }

    public TestResponseEmployeeProposalDaysOffDTO withMonth(Integer month){
        this.month = month;
        return this;
    }

    public TestResponseEmployeeProposalDaysOffDTO withMonthlyDaysOff(int[] monthlyDaysOff){
        this.monthlyDaysOff = monthlyDaysOff;
        return this;
    }

    public TestResponseEmployeeProposalDaysOffDTO withCreatedAt(LocalDateTime createdAt){
        this.createdAt = createdAt;
        return this;
    }

    public TestResponseEmployeeProposalDaysOffDTO withUpdatedAt(LocalDateTime updatedAt){
        this.updatedAt = updatedAt;
        return this;
    }

    public ResponseEmployeeProposalDaysOffDTO build(){
        return new ResponseEmployeeProposalDaysOffDTO(
                id,
                storeId,
                employeeId,
                year,
                month,
                monthlyDaysOff,
                createdAt,
                updatedAt
        );
    }
}
