package online.stworzgrafik.StworzGrafik.employee.proposal.shifts;

import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.DTO.ResponseEmployeeProposalShiftsDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

interface EmployeeProposalShiftsRepository extends JpaRepository<EmployeeProposalShifts,Long>, JpaSpecificationExecutor<EmployeeProposalShifts> {
    boolean existsByStore_IdAndEmployee_IdAndDate(Long storeId, Long employeeId, LocalDate date);

    List<EmployeeProposalShifts> findByStoreIdAndEmployeeId(Long storeId, Long employeeId);

    List<EmployeeProposalShifts> findByStoreIdAndEmployeeIdAndDateBetween(Long storeId, LocalDate startDate, LocalDate endDate, Long employeeId);

    List<EmployeeProposalShifts> findByStoreIdAndDateBetween(Long storeId, LocalDate startDate, LocalDate endDate);
}
