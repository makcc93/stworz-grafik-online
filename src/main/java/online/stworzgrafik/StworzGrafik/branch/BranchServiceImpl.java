package online.stworzgrafik.StworzGrafik.branch;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.branch.DTO.CreateBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.ResponseBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.UpdateBranchDTO;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.RegionRepository;
import online.stworzgrafik.StworzGrafik.validator.NameValidatorService;
import online.stworzgrafik.StworzGrafik.validator.ObjectType;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;

@Service
@Validated
public class BranchServiceImpl implements BranchService{
    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;
    private final BranchBuilder branchBuilder;
    private final RegionRepository regionRepository;
    private final NameValidatorService nameValidatorService;

    public BranchServiceImpl(BranchRepository branchRepository, BranchMapper branchMapper, BranchBuilder branchBuilder, RegionRepository regionRepository, NameValidatorService nameValidatorService) {
        this.branchRepository = branchRepository;
        this.branchMapper = branchMapper;
        this.branchBuilder = branchBuilder;
        this.regionRepository = regionRepository;
        this.nameValidatorService = nameValidatorService;
    }

    @Override
    public ResponseBranchDTO findById(Long id) {
        Objects.requireNonNull(id,"Id cannot be null");

        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Branch with id " + id + " does not exist"));

        return branchMapper.toResponseBranchDTO(branch);
    }

    @Override
    public ResponseBranchDTO createBranch(CreateBranchDTO createBranchDTO) {
        Objects.requireNonNull(createBranchDTO);

        if (branchRepository.existsByName(createBranchDTO.name())){
            throw new EntityExistsException("Branch with name " + createBranchDTO.name() + " already exist");
        }

        String validatedName = nameValidatorService.validate(createBranchDTO.name(), ObjectType.BRANCH);

        Region region = regionRepository.findById(createBranchDTO.regionId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find region by id " + createBranchDTO.regionId()));

        Branch branch = branchBuilder.createBranch(validatedName,region);

        Branch savedBranch = branchRepository.save(branch);

        return branchMapper.toResponseBranchDTO(savedBranch);
    }

    @Override
    public ResponseBranchDTO updateBranch(Long id, UpdateBranchDTO updateBranchDTO) {
        Objects.requireNonNull(id, "Id cannot be null");
        Objects.requireNonNull(updateBranchDTO, "Update branch DTO cannot be null");

        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Branch with id " + id + " does not exist"));

        branchMapper.updateBranchFromDTO(updateBranchDTO, branch);

        Branch savedBranch = branchRepository.save(branch);

        return branchMapper.toResponseBranchDTO(savedBranch);
    }

    @Override
    public void delete(Long id) {
        Objects.requireNonNull(id,"Id cannot be null");

        if (!branchRepository.existsById(id)){
            throw new EntityNotFoundException("Branch with id " + id + " does not exist");
        }

        branchRepository.deleteById(id);
    }

    @Override
    public boolean exists(Long id) {
        Objects.requireNonNull(id,"Id cannot be null");

        return branchRepository.existsById(id);
    }

    @Override
    public boolean exists(String name) {
        Objects.requireNonNull(name, "Name cannot be null");

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
