package online.stworzgrafik.StworzGrafik.draft;

import jakarta.persistence.*;
import lombok.*;
import online.stworzgrafik.StworzGrafik.converter.IntArrayJsonConverter;
import online.stworzgrafik.StworzGrafik.store.Store;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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

    @Column(nullable = false)
    private LocalDateTime created_at;

    private LocalDateTime updated_at;

    @PrePersist
    void onCreate(){
        this.created_at = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate(){
        this.updated_at = LocalDateTime.now();
    }
}
