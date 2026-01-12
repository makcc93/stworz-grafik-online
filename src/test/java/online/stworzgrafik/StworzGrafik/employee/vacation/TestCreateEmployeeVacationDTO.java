package online.stworzgrafik.StworzGrafik.employee.vacation;

import jakarta.persistence.criteria.CriteriaBuilder;
import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.CreateEmployeeVacationDTO;

public class TestCreateEmployeeVacationDTO {
    private Integer year = 2019;
    private Integer month = 9;
    private int[] monthlyVacation = {1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

    public TestCreateEmployeeVacationDTO withYear(Integer year){
        this.year = year;
        return this;
    }

    public TestCreateEmployeeVacationDTO withMonth(Integer month){
        this.month = month;
        return this;
    }

    public TestCreateEmployeeVacationDTO withMonthlyVacation(int[] monthlyVacation){
        this.monthlyVacation = monthlyVacation;
        return this;
    }

    public CreateEmployeeVacationDTO build(){
        return new CreateEmployeeVacationDTO(
                year,
                month,
                monthlyVacation
        );
    }
}
