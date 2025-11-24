package online.stworzgrafik.StworzGrafik.shift;

import jakarta.persistence.*;
import lombok.*;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetails;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalTime startHour;

    private LocalTime endHour;

    @OneToMany(mappedBy = "shift")
    private List<ScheduleDetails> scheduleDetails;
}
