package online.stworzgrafik.StworzGrafik.employee.proposal.daysOff;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface EmployeeProposalDaysOffRepository extends JpaRepository<EmployeeProposalDaysOff,Long>, JpaSpecificationExecutor<EmployeeProposalDaysOff> {
    boolean existsByStoreIdAndEmployeeIdAndYearAndMonth(Long storeId, Long employeeId, Integer year, Integer month);
}
