package online.stworzgrafik.StworzGrafik.employee.delegation.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateEmployeeDelegationDTO(
        @NotNull
        @Min(value = 2000,message = "Year minimum value is 2000")
        @Max(value = 2099, message = "Year maximum value is 2099")
        Integer year,

        @NotNull
        @Min(value = 1,message = "Month minimum value is 1")
        @Max(value = 12, message = "Month maximum value is 12")
        Integer month,

        @NotNull
        @Size(min=31, max = 31, message = "Employee monthly delegation array must have exactly 31 elements")
        int[] monthlyDelegation
) {
}
