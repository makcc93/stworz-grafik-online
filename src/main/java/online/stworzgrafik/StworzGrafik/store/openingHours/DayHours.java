package online.stworzgrafik.StworzGrafik.store.openingHours;
import java.time.LocalTime;

public record DayHours(LocalTime open, LocalTime close){

    public DayHours {
        if (open.isAfter(close)) {
            throw new IllegalArgumentException("openTime must be before closeTime");
        }
    }
}
