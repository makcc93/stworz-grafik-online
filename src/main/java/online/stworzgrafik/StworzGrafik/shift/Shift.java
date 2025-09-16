package online.stworzgrafik.StworzGrafik.shift;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    LocalTime startHour;

    LocalTime endHour;

    public Integer getLength(){
        if (startHour == null || endHour == null){
            throw new IllegalArgumentException("Start or end hour cannot be null");
        }

        if (endHour.isBefore(startHour)){
            throw new IllegalArgumentException("End hour cannot be before start hour");
        }

        return endHour.getHour() - startHour.getHour();
    }
}
