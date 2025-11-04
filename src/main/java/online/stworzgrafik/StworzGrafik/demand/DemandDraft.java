package online.stworzgrafik.StworzGrafik.demand;

import jakarta.persistence.*;
import lombok.*;
import online.stworzgrafik.StworzGrafik.store.Store;

import java.time.LocalTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
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

    private Integer year;

    private Integer month;

    private Integer day;

    private int[] hourlyDemand;

    @Column(nullable = false)
    private LocalTime created_at;

    private LocalTime updated_at;

    @PrePersist
    void onCreate(){
        this.created_at = LocalTime.now();
    }

    @PreUpdate
    void onUpdate(){
        this.updated_at = LocalTime.now();
    }
}
