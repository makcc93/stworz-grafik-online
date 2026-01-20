package online.stworzgrafik.StworzGrafik.region;

import org.springframework.data.jpa.domain.Specification;

class RegionSpecification {
    public static Specification<Region> hasId(Long regionId){
        if (regionId == null) return null;

        return (root,query,criteriaBuilder) ->
                criteriaBuilder.equal(root.get("id"),regionId);
    }

    public static Specification<Region> hasNameLike(String name){
        if (name == null) return null;

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),"%" + name.toLowerCase() + "%");
    }

    public static Specification<Region> isEnable(Boolean enable){
        if (enable == null) return null;

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("enable"),enable);
    }
}
