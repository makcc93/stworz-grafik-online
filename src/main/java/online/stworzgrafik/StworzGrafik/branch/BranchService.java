package online.stworzgrafik.StworzGrafik.branch;

import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.branch.DTO.CreateBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.ResponseBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.UpdateBranchDTO;
import org.springframework.stereotype.Service;

import java.util.List;

public interface BranchService {
    ResponseBranchDTO findById(@NotNull Long id);
    ResponseBranchDTO createBranch(@NotNull  CreateBranchDTO createBranchDTO);
    ResponseBranchDTO updateBranch(@NotNull Long id, @NotNull UpdateBranchDTO updateBranchDTO);
    void delete(@NotNull Long id);
    boolean exists(@NotNull Long id);
    boolean exists(@NotNull String name);
    List<ResponseBranchDTO> findAll();
}
