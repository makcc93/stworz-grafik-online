package online.stworzgrafik.StworzGrafik.branch;

import online.stworzgrafik.StworzGrafik.branch.DTO.CreateBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.ResponseBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.UpdateBranchDTO;

import java.util.List;

public interface BranchService {
    ResponseBranchDTO findById(Long id);
    ResponseBranchDTO createBranch(CreateBranchDTO createBranchDTO);
    ResponseBranchDTO updateBranch(Long id, UpdateBranchDTO updateBranchDTO);
    void delete(Long id);
    boolean exists(Long id);
    boolean exists(String name);
    List<ResponseBranchDTO> findAll();
}
