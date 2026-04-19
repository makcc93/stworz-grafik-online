package online.stworzgrafik.StworzGrafik.algorithm.analyzer.DTO;

import online.stworzgrafik.StworzGrafik.shift.Shift;

public record DividedShiftDTO (
        Shift morningShift,
        Shift afternoonShift
) {
}
