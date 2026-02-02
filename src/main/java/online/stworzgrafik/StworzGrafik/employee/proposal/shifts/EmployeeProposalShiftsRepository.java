package online.stworzgrafik.StworzGrafik.employee.proposal.shifts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
interface EmployeeProposalShiftsRepository extends JpaRepository<EmployeeProposalShifts,Long>, JpaSpecificationExecutor<EmployeeProposalShifts> {
    boolean existsByStore_IdAndEmployee_IdAndDate(Long storeId, Long employeeId, LocalDate date);
}
