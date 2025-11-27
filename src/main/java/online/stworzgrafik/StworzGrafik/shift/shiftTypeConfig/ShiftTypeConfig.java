package online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig;

import jakarta.persistence.*;
import lombok.*;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetails;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class ShiftTypeConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code")
    @Enumerated(EnumType.STRING)
    private ShiftCode code;

    private String namePl;

    private BigDecimal defaultHours;

    private Boolean countsAsWork;

    @OneToMany(mappedBy = "shiftTypeConfig")
    private List<ScheduleDetails> scheduleDetails;
}
