package online.stworzgrafik.StworzGrafik.branch;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.branch.DTO.CreateBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.ResponseBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.UpdateBranchDTO;
import online.stworzgrafik.StworzGrafik.exception.ArgumentNullChecker;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.RegionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BranchServiceImpl implements BranchService{
    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;
    private final BranchBuilder branchBuilder;
    private final RegionRepository regionRepository;

    public BranchServiceImpl(BranchRepository branchRepository, BranchMapper branchMapper, BranchBuilder branchBuilder, RegionRepository regionRepository) {
        this.branchRepository = branchRepository;
        this.branchMapper = branchMapper;
        this.branchBuilder = branchBuilder;
        this.regionRepository = regionRepository;
    }

    @Override
    public ResponseBranchDTO findById(Long id) {
        ArgumentNullChecker.check(id,"Id");

        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Branch with id " + id + " does not exist"));

        return branchMapper.toResponseBranchDTO(branch);
    }

    @Override
    public ResponseBranchDTO createBranch(CreateBranchDTO createBranchDTO) {
        ArgumentNullChecker.check(createBranchDTO);

        if (branchRepository.existsByName(createBranchDTO.name())){
            throw new EntityExistsException("Branch with name " + createBranchDTO.name() + " already exists");
        }

        Region region = regionRepository.findById(createBranchDTO.regionId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find region by id " + createBranchDTO.regionId()));

        Branch branch = branchBuilder.createBranch(createBranchDTO.name(),region);

        Branch savedBranch = branchRepository.save(branch);

        return branchMapper.toResponseBranchDTO(savedBranch);
    }

    @Override
    public ResponseBranchDTO updateBranch(Long id, UpdateBranchDTO updateBranchDTO) {
        ArgumentNullChecker.check(id,"Id");
        ArgumentNullChecker.check(updateBranchDTO);

        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Branch with id " + id + " does not exist"));

        branchMapper.updateBranchFromDTO(updateBranchDTO, branch);

        Branch savedBranch = branchRepository.save(branch);

        return branchMapper.toResponseBranchDTO(savedBranch);
    }

    @Override
    public void delete(Long id) {
        ArgumentNullChecker.check(id,"Id");

        if (!branchRepository.existsById(id)){
            throw new EntityNotFoundException("Branch with id " + id + " does not exist");
        }

        branchRepository.deleteById(id);
    }

    @Override
    public boolean exists(Long id) {
        ArgumentNullChecker.check(id,"Id");

        return branchRepository.existsById(id);
    }

    @Override
    public boolean exists(String name) {
        ArgumentNullChecker.check(name,"Name");

        return branchRepository.existsByName(name);
    }


    @Override
    public List<ResponseBranchDTO> findAll() {
        List<Branch> branches = branchRepository.findAll();

        return branches.stream()
                .map(branchMapper::toResponseBranchDTO)
                .toList();
    }
}
