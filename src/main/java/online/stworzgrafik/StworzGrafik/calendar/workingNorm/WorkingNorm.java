package online.stworzgrafik.StworzGrafik.calendar.workingNorm;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class WorkingNorm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "year_number", nullable = false)
    private Integer year;

    @Column(name = "month_number", nullable = false)
    private Integer month;

    @Column(nullable = false)
    private int hours;

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