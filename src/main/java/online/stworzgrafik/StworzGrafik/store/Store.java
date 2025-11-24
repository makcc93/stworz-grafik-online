package online.stworzgrafik.StworzGrafik.store;

import jakarta.persistence.*;
import lombok.*;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.draft.DemandDraft;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.EmployeeProposalDaysOff;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.EmployeeProposalShifts;
import online.stworzgrafik.StworzGrafik.employee.vacation.EmployeeVacation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
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

    @OneToMany(mappedBy = "store")
    private List<EmployeeVacation> employeeVacations;

    @OneToMany(mappedBy = "store")
    private List<EmployeeProposalDaysOff> employeeProposalDaysOff;

    @OneToMany(mappedBy = "store")
    private List<EmployeeProposalShifts> employeeProposalShifts;

    @PrePersist
    void onCreate(){
        this.createdAt = LocalDateTime.now();
        this.enable = true;
    }
}
