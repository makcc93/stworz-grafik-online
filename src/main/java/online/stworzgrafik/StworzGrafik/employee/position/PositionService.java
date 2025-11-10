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
    public List<ResponsePositionDTO> findAll();
    public ResponsePositionDTO findById(@NotNull Long id);
    public ResponsePositionDTO createPosition(@NotNull CreatePositionDTO createPositionDTO);
    public ResponsePositionDTO updatePosition(@NotNull Long id, @NotNull UpdatePositionDTO updatePositionDTO);
    public ResponsePositionDTO save(@NotNull Position position);
    public void delete(@NotNull Long id);
    public boolean exists(@NotNull Long id);
    public boolean exists(@NotNull String name);
}
