package online.stworzgrafik.StworzGrafik.store;

import jakarta.persistence.*;
import lombok.*;
import online.stworzgrafik.StworzGrafik.branch.Branch;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Getter
@Setter
@Builder
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String storeCode;

    private String name;

    private String location;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Enumerated(EnumType.STRING)
    private RegionType region;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(updatable = true, nullable = false)
    private boolean isEnable;

    private Long storeManagerId;

    private LocalTime openForClientsHour;

    private LocalTime closeForClientsHour;

    @PrePersist
    void onCreate(){
        createdAt = LocalDateTime.now();
        isEnable = true;
    }
}
