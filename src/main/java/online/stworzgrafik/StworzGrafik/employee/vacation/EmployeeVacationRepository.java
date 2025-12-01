package online.stworzgrafik.StworzGrafik.employee.vacation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EmployeeVacationRepository extends JpaRepository<EmployeeVacation, Long>, JpaSpecificationExecutor<EmployeeVacation> {
}
