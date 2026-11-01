package online.stworzgrafik.StworzGrafik.store.modificationHours;

import jakarta.persistence.*;
import lombok.*;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.store.Store;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shift_hour_modification_config")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class ShiftHourModificationConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false, unique = true)
    private Store store;

    @ElementCollection
    @CollectionTable(
            name = "shift_hour_mapping",
            joinColumns = @JoinColumn(name = "config_id")
    )
    @Builder.Default
    private List<ShiftHourRule> rules = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "shift_hour_modification_excluded_employees",
            joinColumns = @JoinColumn(name = "config_id"),
            inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    @Builder.Default
    private List<Employee> excludedEmployees = new ArrayList<>();
}