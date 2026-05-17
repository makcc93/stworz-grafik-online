package online.stworzgrafik.StworzGrafik.employee.workNorm;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "special_work_norm")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class SpecialWorkNorm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal maxDailyHours;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal weeklyNorm;

    private String description;

    private Boolean active;
}
