package online.stworzgrafik.StworzGrafik.employee.vacation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

interface EmployeeVacationRepository extends JpaRepository<EmployeeVacation, Long>, JpaSpecificationExecutor<EmployeeVacation> {
    boolean existsByStore_IdAndEmployee_IdAndYearAndMonth(Long storeId, Long employeeId, Integer year, Integer month);
    Optional<EmployeeVacation> findByStore_IdAndEmployee_IdAndYearAndMonth(Long storeId, Long employeeId, Integer year, Integer month);
    List<EmployeeVacation> findAllByStore_IdAndYearAndMonth(Long storeId, Integer year, Integer month);
}
