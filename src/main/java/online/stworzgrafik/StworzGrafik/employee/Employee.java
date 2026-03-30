package online.stworzgrafik.StworzGrafik.employee;

import jakarta.persistence.*;
import lombok.*;
import online.stworzgrafik.StworzGrafik.employee.position.Position;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.EmployeeProposalDaysOff;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.EmployeeProposalShifts;
import online.stworzgrafik.StworzGrafik.employee.vacation.EmployeeVacation;
import online.stworzgrafik.StworzGrafik.schedule.details.ScheduleDetails;
import online.stworzgrafik.StworzGrafik.store.Store;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
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

    private boolean enable = true;

    private boolean canOperateCheckout = false;

    private boolean canOperateCredit = false;

    private boolean canOpenCloseStore = false;

    private boolean canOperateDelivery = false;

    private boolean seller = false;

    private boolean manager = false;

    private boolean cashier = false;

    private boolean warehouseman = false;

    private boolean pok = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true, updatable = true)
    private LocalDateTime updatedAt = null;

    @OneToMany(mappedBy = "employee")
    private List<EmployeeVacation> employeeVacations;

    @OneToMany(mappedBy = "employee")
    private List<EmployeeProposalDaysOff> employeeProposalDaysOff;

    @OneToMany(mappedBy = "employee")
    private List<EmployeeProposalShifts> employeeProposalShifts;

    @OneToMany(mappedBy = "employee")
    private List<ScheduleDetails> scheduleDetails;

    @PrePersist
    void onCreate(){
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate(){
        updatedAt = LocalDateTime.now();
    }
}
