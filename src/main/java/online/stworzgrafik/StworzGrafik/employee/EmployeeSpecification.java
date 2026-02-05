package online.stworzgrafik.StworzGrafik.employee;

import org.springframework.data.jpa.domain.Specification;

class EmployeeSpecification {
    static Specification<Employee> hasId(Long employeeId){
        if (employeeId == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("id"),employeeId);
    }

    static Specification<Employee> hasFirstNameLike(String firstName){
        if (firstName == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")),"%" + firstName.toLowerCase() + "%");
    }

    static Specification<Employee> hasLastNameLike(String lastName){
        if (lastName == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")),"%" + lastName.toLowerCase() + "%");
    }

    static Specification<Employee> hasSap(Long sap){
        if (sap == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("sap"),sap);
    }

    static Specification<Employee> hasStoreId(Long storeId){
        if (storeId == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("store").get("id"),storeId);
    }

    static Specification<Employee> hasPositionId(Long positionId){
        if (positionId == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("position").get("id"),positionId);
    }

    static Specification<Employee> isEnable(Boolean enable){
        if (enable == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("enable"),enable);
    }

    static Specification<Employee> canOperateCheckout(Boolean canOperateCheckout){
        if (canOperateCheckout == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("canOperateCheckout"),canOperateCheckout);
    }

    static Specification<Employee> canOperateCredit(Boolean canOperateCredit){
        if (canOperateCredit == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("canOperateCredit"),canOperateCredit);
    }

    static Specification<Employee> canOpenCloseStore(Boolean canOpenCloseStore){
        if (canOpenCloseStore == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("canOpenCloseStore"),canOpenCloseStore);
    }

    static Specification<Employee> isSeller(Boolean seller){
        if (seller == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("seller"),seller);
    }

    static Specification<Employee> isManager(Boolean manager){
        if (manager == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("manager"),manager);
    }
}
