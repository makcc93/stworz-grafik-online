package online.stworzgrafik.StworzGrafik.employee.vacation;

import jakarta.persistence.criteria.CriteriaBuilder;
import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.EmployeeVacationSpecificationDTO;

public class TestEmployeeVacationSpecificationDTO {

    private Long employeeId = null;
    private Integer year = null;
    private Integer month = null;

    public TestEmployeeVacationSpecificationDTO withEmployeeId(Long employeeId){
        this.employeeId = employeeId;
        return this;
    }

    public TestEmployeeVacationSpecificationDTO withYear(Integer year){
        this.year = year;
        return this;
    }

    public TestEmployeeVacationSpecificationDTO withMonth(Integer month){
        this.month = month;
        return this;
    }

    public EmployeeVacationSpecificationDTO build(){
        return new EmployeeVacationSpecificationDTO(
                employeeId,
                year,
                month
        );
    }
}
