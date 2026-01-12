package online.stworzgrafik.StworzGrafik.employee.vacation;

import jakarta.persistence.criteria.CriteriaBuilder;
import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.UpdateEmployeeVacationDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class TestUpdateEmployeeVacationDTO {
    private Integer year = 2023;
    private Integer month = 10;
    private int[] monthlyVacation = {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0};
    private LocalDateTime updatedAt = LocalDateTime.now();

    public TestUpdateEmployeeVacationDTO withYear(Integer year){
        this.year = year;
        return this;
    }

    public TestUpdateEmployeeVacationDTO withMonth(Integer month){
        this.month = month;
        return this;
    }

    public TestUpdateEmployeeVacationDTO withMonthlyVacation(int[] monthlyVacation){
        this.monthlyVacation = monthlyVacation;
        return this;
    }

    public UpdateEmployeeVacationDTO build(){
        return new UpdateEmployeeVacationDTO(
                year,
                month,
                monthlyVacation,
                updatedAt
        );
    }
}
