package online.stworzgrafik.StworzGrafik.schedule;

import jakarta.persistence.*;
import lombok.*;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetails;
import online.stworzgrafik.StworzGrafik.store.Store;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "year_number")
    private Integer year;

    @Column(name = "month_number")
    private Integer month;

    @Column(nullable = true)
    private String name;

    private LocalDateTime createdAt;

    private Long createdByUserId;

    @Column(nullable = true)
    private LocalDateTime updatedAt;

    private Long updatedByUserId;

    @Enumerated(EnumType.STRING)
    private ScheduleStatus scheduleStatus;

    @OneToMany(mappedBy = "schedule")
    private List<ScheduleDetails> scheduleDetails;

    @PrePersist
    void onCreate(){
        this.createdAt = LocalDateTime.now();
        this.scheduleStatus = ScheduleStatus.IN_PROGRESS;
    }

    @PreUpdate
    void onUpdate(){
        this.updatedAt = LocalDateTime.now();
    }
}
