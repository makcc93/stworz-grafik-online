package online.stworzgrafik.StworzGrafik.employee;

import org.springframework.data.jpa.domain.Specification;

class EmployeeSpecification {
    public static Specification<Employee> hasId(Long employeeId){
        if (employeeId == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("id"),employeeId);
    }

    public static Specification<Employee> hasFirstNameLike(String firstName){
        if (firstName == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")),"%" + firstName.toLowerCase() + "%");
    }

    public static Specification<Employee> hasLastNameLike(String lastName){
        if (lastName == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")),"%" + lastName.toLowerCase() + "%");
    }

    public static Specification<Employee> hasSap(Long sap){
        if (sap == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("sap"),sap);
    }

    public static Specification<Employee> hasStoreId(Long storeId){
        if (storeId == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("store").get("id"),storeId);
    }

    public static Specification<Employee> hasPositionId(Long positionId){
        if (positionId == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("position").get("id"),positionId);
    }

    public static Specification<Employee> isEnable(Boolean enable){
        if (enable == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("enable"),enable);
    }

    public static Specification<Employee> canOperateCheckout(Boolean canOperateCheckout){
        if (canOperateCheckout == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("canOperateCheckout"),canOperateCheckout);
    }

    public static Specification<Employee> canOperateCredit(Boolean canOperateCredit){
        if (canOperateCredit == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("canOperateCredit"),canOperateCredit);
    }

    public static Specification<Employee> canOpenCloseStore(Boolean canOpenCloseStore){
        if (canOpenCloseStore == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("canOpenCloseStore"),canOpenCloseStore);
    }

    public static Specification<Employee> isSeller(Boolean seller){
        if (seller == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("seller"),seller);
    }

    public static Specification<Employee> isManager(Boolean manager){
        if (manager == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("manager"),manager);
    }
}
