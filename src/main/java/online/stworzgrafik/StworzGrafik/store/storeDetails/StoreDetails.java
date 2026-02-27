package online.stworzgrafik.StworzGrafik.store.storeDetails;

import jakarta.persistence.*;
import lombok.*;
import online.stworzgrafik.StworzGrafik.store.Store;

import java.time.LocalDateTime;

@Entity
@Table(name = "store_details")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class StoreDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "store_id", nullable = false, unique = true)
    private Store store;

    @Embedded
    private StoreOpeningHours hours;

    @Embedded
    private OptimalStaffing staffing;

    private LocalDateTime createdAt;

    private Long createdByUserId;

    private LocalDateTime updatedAt;

    private Long updatedByUserId;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();

        if (hours == null) {
            hours = StoreOpeningHours.createDefault();
        }
        if (staffing == null) {
            staffing = OptimalStaffing.createDefault();
        }
    }

    @PreUpdate
    void onUpdate(){
        updatedAt = LocalDateTime.now();
    }
}