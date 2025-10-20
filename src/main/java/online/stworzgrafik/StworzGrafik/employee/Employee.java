package online.stworzgrafik.StworzGrafik.employee;

import jakarta.persistence.*;
import lombok.*;
import online.stworzgrafik.StworzGrafik.employee.position.Position;
import online.stworzgrafik.StworzGrafik.store.Store;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    private Long sap;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

    private boolean enable;

    private boolean canOperateCheckout;

    private boolean canOperateCredit;

    private boolean canOpenCloseStore;

    private boolean seller;

    private boolean manager;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true, updatable = true)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate(){
        enable = true;
        canOperateCheckout = false;
        canOperateCredit = false;
        canOpenCloseStore = false;
        seller = false;
        manager = false;
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate(){
        updatedAt = LocalDateTime.now();
    }
}
