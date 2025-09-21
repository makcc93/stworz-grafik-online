package online.stworzgrafik.StworzGrafik.branch;

import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.branch.DTO.NameBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.ResponseBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.UpdateBranchDTO;
import online.stworzgrafik.StworzGrafik.exception.ArgumentNullChecker;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BranchServiceImpl implements BranchService{
    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;
    private final BranchBuilder branchBuilder;

    public BranchServiceImpl(BranchRepository branchRepository, BranchMapper branchMapper, BranchBuilder branchBuilder) {
        this.branchRepository = branchRepository;
        this.branchMapper = branchMapper;
        this.branchBuilder = branchBuilder;
    }

    @Override
    public ResponseBranchDTO findById(Long id) {
        ArgumentNullChecker.check(id,"Id");

        Branch branch = branchRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);

        return branchMapper.toResponseBranchDTO(branch);
    }

    @Override
    public ResponseBranchDTO createBranch(NameBranchDTO nameBranchDTO) {
        ArgumentNullChecker.check(nameBranchDTO);

        Branch branch = branchBuilder.createBranch(nameBranchDTO.name());

        Branch savedBranch = branchRepository.save(branch);

        return branchMapper.toResponseBranchDTO(savedBranch);
    }

    @Override
    public ResponseBranchDTO updateBranch(Long id, UpdateBranchDTO updateBranchDTO) {
        ArgumentNullChecker.check(updateBranchDTO);

        Branch branch = branchRepository.findById(id).orElseThrow();

        branchMapper.updateBranchFromDTO(updateBranchDTO, branch);

        Branch savedBranch = branchRepository.save(branch);

        return branchMapper.toResponseBranchDTO(savedBranch);
    }

    @Override
    public void delete(Long id) {
        ArgumentNullChecker.check(id,"Id");

        if (!exists(id)){
            throw new EntityNotFoundException("Branch with id " + id + "does not exist");
        }

        branchRepository.deleteById(id);
    }

    @Override
    public boolean exists(Long id) {
        ArgumentNullChecker.check(id,"Id");

        return branchRepository.existsById(id);
    }

    @Override
    public List<ResponseBranchDTO> findAll() {
        List<Branch> branches = branchRepository.findAll();

        return branches.stream()
                .map(branchMapper::toResponseBranchDTO)
                .toList();
    }
}
