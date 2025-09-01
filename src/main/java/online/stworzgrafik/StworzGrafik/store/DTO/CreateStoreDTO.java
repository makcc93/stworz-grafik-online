package online.stworzgrafik.StworzGrafik.store.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import online.stworzgrafik.StworzGrafik.store.BranchType;
import online.stworzgrafik.StworzGrafik.store.RegionType;

import java.time.LocalTime;

public record CreateStoreDTO(
        @NotBlank(message = "Name is required")
        @Size(min = 3,max = 50,message = "Store name must be between three and fifty chars")
        String name,

        @NotBlank(message = "Store code is required")
        @Size(min = 2,max = 2,message = "Store code must have exactly two chars")
        @Pattern(regexp = "[a-zA-Z0-9]{2}")
        String storeCode,

        @NotNull String location,

        @NotNull BranchType branch,

        @NotNull RegionType region,

        @NotNull LocalTime openForClientsHour,

        @NotNull LocalTime closeForClientsHour
) {
    public CreateStoreDTO{
        validateHours(openForClientsHour,closeForClientsHour);

        if (name != null){
            name = name.trim().toUpperCase();
        }

        if (storeCode != null){
            storeCode = storeCode.trim().toUpperCase();
        }
    }

    private void validateHours(@NotNull LocalTime openForClientsHour, @NotNull LocalTime closeForClientsHour) {
        if (closeForClientsHour.isBefore(openForClientsHour)){
            throw new IllegalArgumentException("Close hour cannot be before open hour");
        }
    }
}
