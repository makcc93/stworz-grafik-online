package online.stworzgrafik.StworzGrafik.employee.proposal.daysOff;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

interface EmployeeProposalDaysOffRepository extends JpaRepository<EmployeeProposalDaysOff,Long>, JpaSpecificationExecutor<EmployeeProposalDaysOff> {
    boolean existsByStore_IdAndEmployee_IdAndYearAndMonth(Long storeId, Long employeeId, Integer year, Integer month);
    Optional<EmployeeProposalDaysOff> findByStore_IdAndEmployee_IdAndYearAndMonth(Long storeId, Long employeeId, Integer year, Integer month);
}
