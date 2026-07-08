package online.stworzgrafik.StworzGrafik.employee.hoursConfirmation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface EmployeeMonthlyHoursConfirmationRepository extends JpaRepository<EmployeeMonthlyHoursConfirmation, Long> {
    Optional<EmployeeMonthlyHoursConfirmation> findByStore_IdAndEmployee_IdAndYearAndMonth(Long storeId, Long employeeId, Integer year, Integer month);
    List<EmployeeMonthlyHoursConfirmation> findAllByStore_IdAndYearAndMonth(Long storeId, Integer year, Integer month);
}