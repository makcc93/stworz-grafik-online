package online.stworzgrafik.StworzGrafik.branch;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import online.stworzgrafik.StworzGrafik.branch.DTO.CreateBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.ResponseBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.UpdateBranchDTO;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface BranchService {
    public ResponseBranchDTO findById(@NotNull Long id);
    public List<ResponseBranchDTO> findAll();
    public ResponseBranchDTO createBranch(@NotNull CreateBranchDTO createBranchDTO);
    public ResponseBranchDTO updateBranch(@NotNull Long id, @NotNull UpdateBranchDTO updateBranchDTO);
    public ResponseBranchDTO save(@NotNull Branch branch);
    public void delete(@NotNull Long id);
    public boolean exists(@NotNull Long id);
    public boolean exists(@NotNull String name);
}
