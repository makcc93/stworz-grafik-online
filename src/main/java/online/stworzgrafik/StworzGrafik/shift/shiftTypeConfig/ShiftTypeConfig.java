package online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Entity
@Getter
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
}
