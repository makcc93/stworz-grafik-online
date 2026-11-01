package online.stworzgrafik.StworzGrafik.store.modificationHours;

import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ShiftHourImpactResponse;
import org.springframework.validation.annotation.Validated;

@Validated
public interface ShiftHourImpactService {
    ShiftHourImpactResponse calculate(@NotNull Long storeId, int year, int month);
}