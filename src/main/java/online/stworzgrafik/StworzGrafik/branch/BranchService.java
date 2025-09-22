package online.stworzgrafik.StworzGrafik.branch;

import online.stworzgrafik.StworzGrafik.branch.DTO.NameBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.ResponseBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.UpdateBranchDTO;

import java.util.List;

public interface BranchService {
    ResponseBranchDTO findById(Long id);
    ResponseBranchDTO createBranch(NameBranchDTO nameBranchDTO);
    ResponseBranchDTO updateBranch(Long id, UpdateBranchDTO updateBranchDTO);
    void delete(Long id);
    boolean exists(Long id);
    boolean exists(String name);
    List<ResponseBranchDTO> findAll();
}
