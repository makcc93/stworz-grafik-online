package online.stworzgrafik.StworzGrafik.store.modificationHours;

import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.store.modificationHours.DTO.ShiftHourModificationDTO;
import org.mapstruct.Mapper;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
interface ShiftHourModificationConfigMapper {

    default List<ShiftHourModificationDTO> toDto(Map<LocalTime, LocalTime> hoursToModify) {
        if (hoursToModify == null) return List.of();
        return hoursToModify.entrySet().stream()
                .map(e -> new ShiftHourModificationDTO(e.getKey(), e.getValue()))
                .toList();
    }

    default Map<LocalTime, LocalTime> toMap(List<ShiftHourModificationDTO> dtos) {
        if (dtos == null) return new HashMap<>();
        return dtos.stream()
                .collect(Collectors.toMap(
                        ShiftHourModificationDTO::originalHour,
                        ShiftHourModificationDTO::modifiedHour
                ));
    }

    default List<Long> toEmployeeIds(List<Employee> employees) {
        if (employees == null) return List.of();
        return employees.stream()
                .map(Employee::getId)
                .toList();
    }
}