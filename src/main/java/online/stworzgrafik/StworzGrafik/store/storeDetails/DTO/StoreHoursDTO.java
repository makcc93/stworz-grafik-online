package online.stworzgrafik.StworzGrafik.store.storeDetails.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record StoreHoursDTO(
        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime mondayOpen,

        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime mondayClose,

        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime tuesdayOpen,

        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime tuesdayClose,
        
        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime wednesdayOpen,

        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime wednesdayClose,

        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime thursdayOpen,

        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime thursdayClose,

        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime fridayOpen,

        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime fridayClose,

        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime saturdayOpen,

        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime saturdayClose,

        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime sundayOpen,

        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime sundayClose
) {}
