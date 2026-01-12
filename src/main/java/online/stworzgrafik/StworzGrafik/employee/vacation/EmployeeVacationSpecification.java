package online.stworzgrafik.StworzGrafik.employee.vacation;

import org.springframework.data.jpa.domain.Specification;
class EmployeeVacationSpecification {
    public static Specification<EmployeeVacation> hasStoreId(Long storeId){
        if (storeId == null) return null;

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("store").get("id"), storeId);
    }

    public static Specification<EmployeeVacation> hasEmployeeId(Long employeeId){
        if (employeeId == null) return null;

        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("employee").get("id"),employeeId));
    }

    public static Specification<EmployeeVacation> hasYear(Integer year){
        if (year == null) return null;

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("year"), year);
    }

    public static Specification<EmployeeVacation> hasMonth(Integer month){
        if (month == null) return null;

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("month"), month);
    }
}
