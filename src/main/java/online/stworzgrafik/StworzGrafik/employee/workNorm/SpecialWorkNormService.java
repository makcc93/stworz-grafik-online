package online.stworzgrafik.StworzGrafik.employee.workNorm;

import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.employee.workNorm.DTO.CreateSpecialWorkNormDTO;
import online.stworzgrafik.StworzGrafik.employee.workNorm.DTO.ResponseSpecialWorkNormDTO;
import online.stworzgrafik.StworzGrafik.employee.workNorm.DTO.UpdateSpecialWorkNormDTO;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface SpecialWorkNormService {
    List<ResponseSpecialWorkNormDTO> findAll();
    List<ResponseSpecialWorkNormDTO> findAllActive();
    ResponseSpecialWorkNormDTO findById(@NotNull Long id);
    ResponseSpecialWorkNormDTO create(@NotNull @Validated CreateSpecialWorkNormDTO dto);
    ResponseSpecialWorkNormDTO update(@NotNull Long id, @NotNull @Validated UpdateSpecialWorkNormDTO dto);
    void delete(@NotNull  Long id);
}
