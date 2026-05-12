package online.stworzgrafik.StworzGrafik.shift;

import online.stworzgrafik.StworzGrafik.shift.DTO.ResponseShiftDTO;
import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftHoursDTO;
import org.mapstruct.*;

import java.math.BigDecimal;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface ShiftMapper {

    @Mapping(target = "length",expression = "java(getShiftLength(shift))")
    ResponseShiftDTO toShiftDto(Shift shift);

    Shift toEntity(ShiftHoursDTO shiftHoursDTO);

    Shift toEntity(ResponseShiftDTO responseShiftDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateShift(ShiftHoursDTO shiftHoursDTO, @MappingTarget Shift shift);

    default BigDecimal getShiftLength(Shift shift) {
        long minutes = java.time.Duration.between(shift.getStartHour(), shift.getEndHour()).toMinutes();
        if (minutes < 0) minutes += 24 * 60;
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
    }
}