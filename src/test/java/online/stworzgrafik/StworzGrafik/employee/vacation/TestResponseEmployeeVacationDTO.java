package online.stworzgrafik.StworzGrafik.employee.vacation;

import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.ResponseEmployeeVacationDTO;

import java.time.LocalDateTime;

public class TestResponseEmployeeVacationDTO {
    private Long id = 100L;
    private Long storeId = 12345L;
    private Long employeeId = 54321L;
    private Integer year = 2010;
    private Integer month = 3;
    private int[] monthlyVacation = {1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1};
    private LocalDateTime createdAt = LocalDateTime.of(2010,10,10,10,10);
    private LocalDateTime updatedAt = LocalDateTime.of(2020,10,20,20,20);

    public TestResponseEmployeeVacationDTO withId(Long id){
        this.id = id;
        return this;
    }

    public TestResponseEmployeeVacationDTO withStoreId(Long storeId){
        this.storeId = storeId;
        return this;
    }

    public TestResponseEmployeeVacationDTO withEmployeeId(Long employeeId){
        this.employeeId = employeeId;
        return this;
    }

    public TestResponseEmployeeVacationDTO withYear(Integer year){
        this.year = year;
        return this;
    }

    public TestResponseEmployeeVacationDTO withMonth(Integer month){
        this.month = month;
        return this;
    }

    public TestResponseEmployeeVacationDTO withMonthlyVacation(int[] monthlyVacation){
        this.monthlyVacation = monthlyVacation;
        return this;
    }

    public TestResponseEmployeeVacationDTO withCreatedAt(LocalDateTime createdAt){
        this.createdAt = createdAt;
        return this;
    }

    public TestResponseEmployeeVacationDTO withUpdatedAt(LocalDateTime updatedAt){
        this.updatedAt = updatedAt;
        return this;
    }

    public ResponseEmployeeVacationDTO build(){
        return new ResponseEmployeeVacationDTO(
                id,
                storeId,
                employeeId,
                year,
                month,
                monthlyVacation,
                createdAt,
                updatedAt
        );
    }
}

