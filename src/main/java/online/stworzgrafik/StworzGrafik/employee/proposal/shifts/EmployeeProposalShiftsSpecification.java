package online.stworzgrafik.StworzGrafik.employee.proposal.shifts;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

class EmployeeProposalShiftsSpecification {
    public static Specification<EmployeeProposalShifts> hasStoreId(Long storeId) {
        if (storeId == null) return null;

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("store").get("id"), storeId);
    }

    public static Specification<EmployeeProposalShifts> hasEmployeeId(Long employeeId) {
        if (employeeId == null) return null;

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("employee").get("id"), employeeId);
    }

    public static Specification<EmployeeProposalShifts> isBetweenDates(LocalDate startDate, LocalDate endDate){
        if (startDate == null) return null;

        return (root, query, criteriaBuilder) -> {
            if (endDate == null) return criteriaBuilder.equal(root.get("date"), startDate);

            return criteriaBuilder.between(root.get("date"), startDate,endDate);
        };
    }
}
