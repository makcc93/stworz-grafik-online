package online.stworzgrafik.StworzGrafik.store.modificationHours;

import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ShiftHourModificationDTO;
import org.mapstruct.Mapper;

import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring")
interface ShiftHourModificationConfigMapper {

    default List<ShiftHourModificationDTO> toDto(List<ShiftHourRule> rules) {
        if (rules == null) return List.of();
        return rules.stream()
                .sorted(Comparator.comparing(ShiftHourRule::getOriginalHour).thenComparing(ShiftHourRule::getScope))
                .map(rule -> new ShiftHourModificationDTO(rule.getOriginalHour(), rule.getModifiedHour(), rule.getScope()))
                .toList();
    }

    default List<ShiftHourRule> toRules(List<ShiftHourModificationDTO> dtos) {
        if (dtos == null) return List.of();
        return dtos.stream()
                .map(dto -> new ShiftHourRule(dto.originalHour(), dto.modifiedHour(), dto.scope()))
                .toList();
    }

    default List<Long> toEmployeeIds(List<Employee> employees) {
        if (employees == null) return List.of();
        return employees.stream()
                .map(Employee::getId)
                .toList();
    }
}