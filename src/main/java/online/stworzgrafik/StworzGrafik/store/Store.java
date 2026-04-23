package online.stworzgrafik.StworzGrafik.store;

import jakarta.persistence.*;
import lombok.*;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.draft.DemandDraft;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.EmployeeProposalDaysOff;
import online.stworzgrafik.StworzGrafik.employee.proposal.shifts.EmployeeProposalShifts;
import online.stworzgrafik.StworzGrafik.employee.vacation.EmployeeVacation;
import online.stworzgrafik.StworzGrafik.store.delivery.StoreDelivery;
import online.stworzgrafik.StworzGrafik.store.openingHours.DayHours;
import online.stworzgrafik.StworzGrafik.store.openingHours.StoreOpeningHours;
import online.stworzgrafik.StworzGrafik.store.storeDetails.StoreDetails;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Builder.Default
    @OneToMany(mappedBy = "store")
    private List<Employee> employees = new ArrayList<>();

    @Builder.Default
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

    @OneToOne(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private StoreDetails details;

    @OneToOne(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private StoreDelivery delivery;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreOpeningHours> openingHours = new ArrayList<>();

    public Map<DayOfWeek, DayHours> getOpeningHoursAsMap() {
        return openingHours.stream()
                .collect(Collectors.toMap(
                        StoreOpeningHours::getDayOfWeek,
                        h -> new DayHours(h.getOpenTime(), h.getCloseTime())
                ));
    }

    public void setDetails(StoreDetails details) {
        if (details == null) {
            if (this.details != null) {
                this.details.setStore(null);
            }
        } else {
            details.setStore(this);
        }
        this.details = details;
    }

    public void setDelivery(StoreDelivery delivery) {
        if (delivery == null) {
            if (this.delivery != null) {
                this.delivery.setStore(null);
            }
        } else {
            delivery.setStore(this);
        }
        this.delivery = delivery;
    }

    @PrePersist
    void onCreate(){
        this.createdAt = LocalDateTime.now();
        this.enable = true;
    }
}
