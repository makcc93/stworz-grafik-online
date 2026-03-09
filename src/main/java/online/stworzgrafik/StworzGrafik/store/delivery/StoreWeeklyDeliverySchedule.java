package online.stworzgrafik.StworzGrafik.store.delivery;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.util.EnumMap;
import java.util.Map;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StoreWeeklyDeliverySchedule {

    @Convert(converter = WeeklyScheduleConverter.class)
    @Column(name = "weekly_schedule")
    private Map<DayOfWeek, DayDeliveryConfig> deliverySchedule;

    public DayDeliveryConfig getConfigFor(DayOfWeek dayOfWeek){
        return deliverySchedule.get(dayOfWeek);
    }

    public static StoreWeeklyDeliverySchedule createDefault(){
        int[] workday = {0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0};
        int[] weekend = new int[24];

        Map<DayOfWeek,DayDeliveryConfig> map = new EnumMap<>(DayOfWeek.class);
        map.put(DayOfWeek.MONDAY, new DayDeliveryConfig(true,workday));
        map.put(DayOfWeek.TUESDAY, new DayDeliveryConfig(true,workday));
        map.put(DayOfWeek.WEDNESDAY, new DayDeliveryConfig(true,workday));
        map.put(DayOfWeek.THURSDAY, new DayDeliveryConfig(true,workday));
        map.put(DayOfWeek.FRIDAY, new DayDeliveryConfig(true,workday));
        map.put(DayOfWeek.SATURDAY, new DayDeliveryConfig(false,weekend));
        map.put(DayOfWeek.SUNDAY, new DayDeliveryConfig(false,weekend));

        return new StoreWeeklyDeliverySchedule(map);
    }
}
