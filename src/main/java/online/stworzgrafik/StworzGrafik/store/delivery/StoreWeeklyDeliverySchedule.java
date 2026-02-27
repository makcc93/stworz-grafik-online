package online.stworzgrafik.StworzGrafik.store.delivery;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.*;
import online.stworzgrafik.StworzGrafik.converter.IntArrayJsonConverter;

@Embeddable
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StoreWeeklyDeliverySchedule {

    @Column(name = "monday_delivery")
    private boolean mondayDelivery;

    @Column(name = "monday_shift")
    @Convert(converter = IntArrayJsonConverter.class)
    private int[] mondayShiftAsArray;

    @Column(name = "tuesday_delivery")
    private boolean tuesdayDelivery;

    @Column(name = "tuesday_shift")
    @Convert(converter = IntArrayJsonConverter.class)
    private int[] tuesdayShiftAsArray;

    @Column(name = "wednesday_delivery")
    private boolean wednesdayDelivery;

    @Column(name = "wednesday_shift")
    @Convert(converter = IntArrayJsonConverter.class)
    private int[] wednesdayShiftAsArray;

    @Column(name = "thursday_delivery")
    private boolean thursdayDelivery;

    @Column(name = "thursday_shift")
    @Convert(converter = IntArrayJsonConverter.class)
    private int[] thursdayShiftAsArray;

    @Column(name = "friday_delivery")
    private boolean fridayDelivery;

    @Column(name = "friday_shift")
    @Convert(converter = IntArrayJsonConverter.class)
    private int[] fridayShiftAsArray;

    @Column(name = "saturday_delivery")
    private boolean saturdayDelivery;

    @Column(name = "saturday_shift")
    @Convert(converter = IntArrayJsonConverter.class)
    private int[] saturdayShiftAsArray;

    @Column(name = "sunday_delivery")
    private boolean sundayDelivery;

    @Column(name = "sunday_shift")
    @Convert(converter = IntArrayJsonConverter.class)
    private int[] sundayShiftAsArray;

    public static StoreWeeklyDeliverySchedule createDefault(){
        return StoreWeeklyDeliverySchedule.builder()
                .mondayDelivery(true)
                .mondayShiftAsArray(new int[]{0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0})
                .tuesdayDelivery(true)
                .tuesdayShiftAsArray(new int[]{0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0})
                .wednesdayDelivery(true)
                .wednesdayShiftAsArray(new int[]{0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0})
                .thursdayDelivery(true)
                .thursdayShiftAsArray(new int[]{0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0})
                .fridayDelivery(true)
                .fridayShiftAsArray(new int[]{0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0})
                .saturdayDelivery(false)
                .saturdayShiftAsArray(new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0})
                .sundayDelivery(false)
                .sundayShiftAsArray(new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0})
                .build();
    }
}
