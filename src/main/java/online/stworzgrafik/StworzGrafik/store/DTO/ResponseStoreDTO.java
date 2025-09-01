package online.stworzgrafik.StworzGrafik.store.DTO;

import online.stworzgrafik.StworzGrafik.store.BranchType;
import online.stworzgrafik.StworzGrafik.store.RegionType;
import online.stworzgrafik.StworzGrafik.store.Store;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record ResponseStoreDTO(
    Long id,
    String name,
    String storeCode,
    String location,
    BranchType branch,
    RegionType region,
    LocalDateTime createdAt,
    Boolean isEnable,
    Long storeManagerId,
    LocalTime openForClientsHour,
    LocalTime closeForClientsHour
) {
    public static ResponseStoreDTO from (Store store){
        return new ResponseStoreDTO(
                store.getId(),
                store.getName(),
                store.getStoreCode(),
                store.getLocation(),
                store.getBranch(),
                store.getRegion(),
                store.getCreatedAt(),
                store.getIsEnable(),
                store.getStoreManagerId(),
                store.getOpenForClientsHour(),
                store.getCloseForClientsHour()
        );
    }
}
