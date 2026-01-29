package online.stworzgrafik.StworzGrafik.store.storeDetails;

import jakarta.persistence.*;
import lombok.*;
import online.stworzgrafik.StworzGrafik.store.Store;

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
    private StoreHours hours;

    @Embedded
    private OptimalStaffing staffing;

    @PrePersist
    void onCreate() {
        if (hours == null) {
            hours = StoreHours.createDefault();
        }
        if (staffing == null) {
            staffing = OptimalStaffing.createDefault();
        }
    }
}