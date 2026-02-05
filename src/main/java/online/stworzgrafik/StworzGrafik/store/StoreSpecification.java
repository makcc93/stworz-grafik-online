package online.stworzgrafik.StworzGrafik.store;

import org.springframework.data.jpa.domain.Specification;

class StoreSpecification {
    static Specification<Store> hasStoreCode(String storeCode){
        if (storeCode == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("storeCode"),storeCode);
    }

    static Specification<Store> hasNameLike(String name){
        if (name == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),"%" + name.toLowerCase() + "%");
    }
    static Specification<Store> hasLocationLike(String location){
        if (location == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("location")), "%" + location.toLowerCase() + "%");
    }

    static Specification<Store> hasBranchId(Long branchId){
        if (branchId == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("branch").get("id"),branchId);
    }

    static Specification<Store> hasStoreManagerId(Long storeManagerId){
        if (storeManagerId == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("storeManagerId"),storeManagerId);
    }

    static Specification<Store> isEnable(Boolean enable){
        if (enable == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("enable"),enable);
    }
}
