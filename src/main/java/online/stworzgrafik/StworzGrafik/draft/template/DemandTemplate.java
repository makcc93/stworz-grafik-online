package online.stworzgrafik.StworzGrafik.draft.template;

import jakarta.persistence.*;
import lombok.*;
import online.stworzgrafik.StworzGrafik.converter.IntArrayJsonConverter;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(
        name = "demand_template",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_demand_template",
                columnNames = {"store_id", "year_number", "month_number", "day_of_week"}
        )
)
public class DemandTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false, updatable = false)
    private Long storeId;

    @Column(name = "year_number", nullable = false, updatable = false)
    private Integer year;

    @Column(name = "month_number", nullable = false, updatable = false)
    private Integer month;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, updatable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "hourly_demand", nullable = false)
    @Convert(converter = IntArrayJsonConverter.class)
    private int[] hourlyDemand;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by_user_id", updatable = false)
    private Long createdByUserId;

    @Column(name = "created_by_label", nullable = false, updatable = false)
    private String createdByLabel;

    private LocalDateTime updatedAt;

    @Column(name = "updated_by_user_id")
    private Long updatedByUserId;

    @Column(name = "updated_by_label")
    private String updatedByLabel;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}