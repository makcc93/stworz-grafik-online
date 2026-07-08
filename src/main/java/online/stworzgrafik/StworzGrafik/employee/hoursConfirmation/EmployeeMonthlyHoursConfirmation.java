package online.stworzgrafik.StworzGrafik.employee.hoursConfirmation;

import jakarta.persistence.*;
import lombok.*;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.store.Store;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "employee_monthly_hours_confirmation",
        uniqueConstraints = @UniqueConstraint(columnNames = {"store_id", "employee_id", "year_number", "month_number"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class EmployeeMonthlyHoursConfirmation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "year_number", nullable = false)
    private Integer year;

    @Column(name = "month_number", nullable = false)
    private Integer month;

    @Column(name = "confirmed_hours", nullable = false)
    private BigDecimal confirmedHours;

    private LocalDateTime createdAt;

    @Column(nullable = true)
    private LocalDateTime updatedAt;

    @Column(name = "created_by_user_id", nullable = false, updatable = false)
    private Long createdByUserId;

    @Column(name = "created_by_label", nullable = false, updatable = false)
    private String createdByLabel;

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