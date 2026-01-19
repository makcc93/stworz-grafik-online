package online.stworzgrafik.StworzGrafik.store.DTO;

public record StoreSpecificationDTO(
        String storeCode,
        String name,
        String location,
        Long branchId,
        Long storeManagerId,
        Boolean enable
) {}
