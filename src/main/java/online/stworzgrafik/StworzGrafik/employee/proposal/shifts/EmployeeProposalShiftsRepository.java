package online.stworzgrafik.StworzGrafik.employee.proposal.shifts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EmployeeProposalShiftsRepository extends JpaRepository<EmployeeProposalShifts,Long>, JpaSpecificationExecutor<EmployeeProposalShifts> {
}
