package online.stworzgrafik.StworzGrafik.employee.position;

import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.CreatePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.ResponsePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.UpdatePositionDTO;

import java.util.List;

public interface PositionService {
    List<ResponsePositionDTO> findAll();
    ResponsePositionDTO findById(@NotNull Long id);
    ResponsePositionDTO createPosition(@NotNull CreatePositionDTO createPositionDTO);
    ResponsePositionDTO updatePosition(@NotNull Long id, @NotNull UpdatePositionDTO updatePositionDTO);
    void deletePosition(@NotNull Long id);
    boolean exists(@NotNull Long id);
    boolean exists(@NotNull String name);
}
