package online.stworzgrafik.StworzGrafik.store.modificationHours;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ShiftHourRule {

    @Column(name = "original_hour", nullable = false)
    private LocalTime originalHour;

    @Column(name = "modified_hour", nullable = false)
    private LocalTime modifiedHour;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, length = 10)
    private ShiftHourScope scope = ShiftHourScope.BOTH;
}