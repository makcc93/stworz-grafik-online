package online.stworzgrafik.StworzGrafik.store.modificationHours;

import jakarta.persistence.*;
import lombok.*;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.store.Store;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @MapKeyColumn(name = "original_hour")
    @Column(name = "modified_hour")
    @Builder.Default
    private Map<LocalTime, LocalTime> hoursToModify = new HashMap<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "shift_hour_modification_excluded_employees",
            joinColumns = @JoinColumn(name = "config_id"),
            inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    @Builder.Default
    private List<Employee> excludedEmployees = new ArrayList<>();
}