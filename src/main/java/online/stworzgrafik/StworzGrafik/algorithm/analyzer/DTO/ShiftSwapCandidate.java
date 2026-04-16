package online.stworzgrafik.StworzGrafik.algorithm.analyzer.DTO;

import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.shift.Shift;

import java.time.LocalDate;

public record ShiftSwapCandidate(
        Employee originalEmployee,
        Employee employeeForSwapShift,
        LocalDate originalDateForSwap,
        LocalDate otherEmployeeDateForSwap,
        Shift swappingShift
) {
}
