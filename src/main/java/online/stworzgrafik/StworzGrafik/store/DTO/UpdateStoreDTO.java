package online.stworzgrafik.StworzGrafik.store.DTO;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.store.RegionType;

import java.time.LocalTime;

public record UpdateStoreDTO(
        @Size(min = 3,max = 50,message = "Store name must be between three and fifty chars")
        String name,

        @Size(min = 2,max = 2,message = "Store code must have exactly two chars")
        @Pattern(regexp = "[a-zA-Z0-9]{2}")
        String storeCode,

        @Size(min = 3)
        String location,

        Branch branch,

        RegionType region,

        Boolean isEnable,

        Long storeManagerId,

        LocalTime openForClientsHour,

        LocalTime closeForClientsHour
) {}
