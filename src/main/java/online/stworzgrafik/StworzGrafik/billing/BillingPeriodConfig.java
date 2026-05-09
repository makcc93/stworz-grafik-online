package online.stworzgrafik.StworzGrafik.billing;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "billing_period_config")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class BillingPeriodConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_month", nullable = false, unique = true)
    private int startMonth;

    @Column(name = "duration_months", nullable = false)
    private int durationMonths;
}
