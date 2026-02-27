package online.stworzgrafik.StworzGrafik.employee.position;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.CreatePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.ResponsePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.UpdatePositionDTO;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface PositionService {
    List<ResponsePositionDTO> findAll();
    ResponsePositionDTO findById(@NotNull Long id);
    ResponsePositionDTO createPosition(@NotNull @Valid CreatePositionDTO createPositionDTO);
    ResponsePositionDTO updatePosition(@NotNull Long id, @NotNull @Valid UpdatePositionDTO updatePositionDTO);
    ResponsePositionDTO save(@NotNull Position position);
    void delete(@NotNull Long id);
    boolean exists(@NotNull Long id);
    boolean exists(@NotNull String name);
}
