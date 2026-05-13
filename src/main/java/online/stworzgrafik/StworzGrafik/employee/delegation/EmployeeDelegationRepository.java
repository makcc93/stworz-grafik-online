package online.stworzgrafik.StworzGrafik.employee.delegation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

interface EmployeeDelegationRepository extends JpaRepository<EmployeeDelegation, Long>, JpaSpecificationExecutor<EmployeeDelegation> {
    boolean existsByStore_IdAndEmployee_IdAndYearAndMonth(Long storeId, Long employeeId, Integer year, Integer month);
    Optional<EmployeeDelegation> findByStore_IdAndEmployee_IdAndYearAndMonth(Long storeId, Long employeeId, Integer year, Integer month);
    List<EmployeeDelegation> findAllByStore_IdAndYearAndMonth(Long storeId, Integer year, Integer month);
}
