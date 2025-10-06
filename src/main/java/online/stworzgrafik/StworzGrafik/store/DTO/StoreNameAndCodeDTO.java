package online.stworzgrafik.StworzGrafik.store.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StoreNameAndCodeDTO(
        @NotBlank(message = "Name is required")
        @Size(min = 3,max = 50,message = "Store name must be between three and fifty chars")
        String name,

        @NotBlank(message = "Store code is required")
        @Size(min = 2,max = 2,message = "Store code must have exactly two chars")
        @Pattern(regexp = "[a-zA-Z0-9]{2}")
        String storeCode
) {}