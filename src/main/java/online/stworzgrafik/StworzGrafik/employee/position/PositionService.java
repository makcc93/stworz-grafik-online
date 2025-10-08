package online.stworzgrafik.StworzGrafik.employee.position;

import online.stworzgrafik.StworzGrafik.employee.position.DTO.CreatePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.ResponsePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.UpdatePositionDTO;

import java.util.List;

public interface PositionService {
    List<ResponsePositionDTO> findAll();
    ResponsePositionDTO findById(Long id);
    ResponsePositionDTO createPosition(CreatePositionDTO createPositionDTO);
    ResponsePositionDTO updatePosition(UpdatePositionDTO updatePositionDTO);
    void deletePosition(Long id);
    boolean exists(Long id);
    boolean exists(String name);
}
