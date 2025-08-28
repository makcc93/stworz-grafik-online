package online.stworzgrafik.StworzGrafik.shift;

import online.stworzgrafik.StworzGrafik.shift.DTO.ResponseShiftDTO;
import online.stworzgrafik.StworzGrafik.shift.DTO.ShiftHoursDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface ShiftMapper {

    @Mapping(target = "length",expression = "java(shift.getLength())")
    ResponseShiftDTO toShiftDto(Shift shift);

    Shift toEntity(ShiftHoursDTO shiftHoursDTO);
}