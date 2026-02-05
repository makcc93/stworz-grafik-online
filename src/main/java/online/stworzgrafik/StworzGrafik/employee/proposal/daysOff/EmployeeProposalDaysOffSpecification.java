package online.stworzgrafik.StworzGrafik.employee.proposal.daysOff;

import org.springframework.data.jpa.domain.Specification;

class EmployeeProposalDaysOffSpecification {
    static Specification<EmployeeProposalDaysOff> hasStoreId(Long storeId){
        if (storeId == null) return null;

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("store").get("id"), storeId);
    }

    static Specification<EmployeeProposalDaysOff> hasEmployeeId(Long employeeId){
        if (employeeId == null) return null;

        return ((root, query, criteriaBuilder) ->
           criteriaBuilder.equal(root.get("employee").get("id"),employeeId));
    }

    static Specification<EmployeeProposalDaysOff> hasYear(Integer year){
        if (year == null) return null;

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("year"), year);
    }

    static Specification<EmployeeProposalDaysOff> hasMonth(Integer month){
        if (month == null) return null;

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("month"), month);
    }
}
