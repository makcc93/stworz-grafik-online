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
public class DemandDraft {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    private LocalDate draftDate;

    @Column(nullable = true)
    @Convert(converter = IntArrayJsonConverter.class)
    private int[] hourlyDemand;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = true)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate(){
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate(){
        this.updatedAt = LocalDateTime.now();
    }
}
