package online.stworzgrafik.StworzGrafik.store.storeDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.LocalTime;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
class StoreHours {

    @Column(name = "monday_open")
    private LocalTime mondayOpen;

    @Column(name = "monday_close")
    private LocalTime mondayClose;

    // Tuesday
    @Column(name = "tuesday_open")
    private LocalTime tuesdayOpen;

    @Column(name = "tuesday_close")
    private LocalTime tuesdayClose;

    @Column(name = "wednesday_open")
    private LocalTime wednesdayOpen;

    @Column(name = "wednesday_close")
    private LocalTime wednesdayClose;

    @Column(name = "thursday_open")
    private LocalTime thursdayOpen;

    @Column(name = "thursday_close")
    private LocalTime thursdayClose;

    @Column(name = "friday_open")
    private LocalTime fridayOpen;

    @Column(name = "friday_close")
    private LocalTime fridayClose;

    @Column(name = "saturday_open")
    private LocalTime saturdayOpen;

    @Column(name = "saturday_close")
    private LocalTime saturdayClose;

    @Column(name = "sunday_open")
    private LocalTime sundayOpen;

    @Column(name = "sunday_close")
    private LocalTime sundayClose;

    public static StoreHours createDefault() {
        return StoreHours.builder()
                .mondayOpen(LocalTime.of(8, 0))
                .mondayClose(LocalTime.of(20, 0))
                .tuesdayOpen(LocalTime.of(8, 0))
                .tuesdayClose(LocalTime.of(20, 0))
                .wednesdayOpen(LocalTime.of(8, 0))
                .wednesdayClose(LocalTime.of(20, 0))
                .thursdayOpen(LocalTime.of(8, 0))
                .thursdayClose(LocalTime.of(20, 0))
                .fridayOpen(LocalTime.of(8, 0))
                .fridayClose(LocalTime.of(20, 0))
                .saturdayOpen(LocalTime.of(9, 0))
                .saturdayClose(LocalTime.of(18, 0))
                .sundayOpen(LocalTime.of(10, 0))
                .sundayClose(LocalTime.of(16, 0))
                .build();
    }
}