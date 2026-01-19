package online.stworzgrafik.StworzGrafik.store;

import org.springframework.data.jpa.domain.Specification;

class StoreSpecification {
    public static Specification<Store> hasStoreCode(String storeCode){
        if (storeCode == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("storeCode"),storeCode);
    }

    public static Specification<Store> hasNameLike(String name){
        if (name == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),"%" + name.toLowerCase() + "%");
    }
    public static Specification<Store> hasLocationLike(String location){
        if (location == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("location")), "%" + location.toLowerCase() + "%");
    }

    public static Specification<Store> hasBranchId(Long branchId){
        if (branchId == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("branch").get("id"),branchId);
    }

    public static Specification<Store> hasStoreManagerId(Long storeManagerId){
        if (storeManagerId == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("storeManagerId"),storeManagerId);
    }

    public static Specification<Store> isEnable(Boolean enable){
        if (enable == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("enable"),enable);
    }
}
