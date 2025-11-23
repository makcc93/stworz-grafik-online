package online.stworzgrafik.StworzGrafik.employee.proposal.daysOff;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface EmployeeProposalDaysOffRepository extends JpaRepository<EmployeeProposalDaysOff,Long>, JpaSpecificationExecutor<EmployeeProposalDaysOff> {
}
