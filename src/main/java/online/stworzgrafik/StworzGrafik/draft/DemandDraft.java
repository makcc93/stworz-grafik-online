package online.stworzgrafik.StworzGrafik.draft;

import jakarta.persistence.*;
import lombok.*;
import online.stworzgrafik.StworzGrafik.converter.IntArrayJsonConverter;
import online.stworzgrafik.StworzGrafik.store.Store;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@Setter
@Table(
        name = "demand_draft",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_store_draft_date",
                columnNames = {"store_id", "draft_date"}
        )
)
public class DemandDraft {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "draft_date")
    private LocalDate draftDate;

    @Column(nullable = true)
    @Convert(converter = IntArrayJsonConverter.class)
    private int[] hourlyDemand;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = true)
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
    void onCreate(){
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate(){
        this.updatedAt = LocalDateTime.now();
    }
}
