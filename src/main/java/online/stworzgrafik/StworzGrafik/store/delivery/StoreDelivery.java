package online.stworzgrafik.StworzGrafik.store.delivery;

import jakarta.persistence.*;
import lombok.*;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.store.Store;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreDelivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "store_id", nullable = false, unique = true)
    private Store store;

    private Boolean hasDedicatedWarehouseman;

    @ManyToOne
    @JoinColumn(name = "primary_employee_id", nullable = true)
    private Employee primaryEmployee;

    @Embedded
    private StoreWeeklyDeliverySchedule storeWeeklyDeliverySchedule;

    private LocalDateTime createdAt;

    private Long createdByUserId;

    private LocalDateTime updatedAt;

    private Long updatedByUserId;

    @PrePersist
    void onCreate(){
        createdAt = LocalDateTime.now();
        this.storeWeeklyDeliverySchedule = StoreWeeklyDeliverySchedule.createDefault();
    }

    @PreUpdate
    void onUpdate(){
        updatedAt = LocalDateTime.now();
    }
}
