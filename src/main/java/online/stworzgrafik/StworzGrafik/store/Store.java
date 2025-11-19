package online.stworzgrafik.StworzGrafik.store;

import jakarta.persistence.*;
import lombok.*;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.demandDraft.DemandDraft;
import online.stworzgrafik.StworzGrafik.employee.Employee;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "store")
    private List<Employee> employees = new ArrayList<>();

    @OneToMany(mappedBy = "store")
    private List<DemandDraft> drafts = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(updatable = true, nullable = false)
    private boolean enable;

    private Long storeManagerId;

    @PrePersist
    void onCreate(){
        this.createdAt = LocalDateTime.now();
        this.enable = true;
    }
}
