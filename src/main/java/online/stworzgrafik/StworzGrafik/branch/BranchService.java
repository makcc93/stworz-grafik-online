package online.stworzgrafik.StworzGrafik.branch;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.branch.DTO.CreateBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.ResponseBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.UpdateBranchDTO;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.RegionService;
import online.stworzgrafik.StworzGrafik.validator.NameValidatorService;
import online.stworzgrafik.StworzGrafik.validator.ObjectType;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;

@Service
@Validated
public class BranchService {
    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;
    private final BranchBuilder branchBuilder;
    private final NameValidatorService nameValidatorService;
    private final RegionService regionService;
    private final EntityManager entityManager;

    public BranchService(BranchRepository branchRepository, BranchMapper branchMapper, BranchBuilder branchBuilder, NameValidatorService nameValidatorService, RegionService regionService, EntityManager entityManager) {
        this.branchRepository = branchRepository;
        this.branchMapper = branchMapper;
        this.branchBuilder = branchBuilder;
        this.nameValidatorService = nameValidatorService;
        this.regionService = regionService;
        this.entityManager = entityManager;
    }

    public ResponseBranchDTO findById(Long id) {
        Objects.requireNonNull(id,"Id cannot be null");

        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Branch with id " + id + " does not exist"));

        return branchMapper.toResponseBranchDTO(branch);
    }

    public ResponseBranchDTO createBranch(CreateBranchDTO createBranchDTO) {
        Objects.requireNonNull(createBranchDTO);

        if (branchRepository.existsByName(createBranchDTO.name())){
            throw new EntityExistsException("Branch with name " + createBranchDTO.name() + " already exist");
        }

        String validatedName = nameValidatorService.validate(createBranchDTO.name(), ObjectType.BRANCH);

        Region region = getRegionReference(createBranchDTO);

        Branch branch = branchBuilder.createBranch(validatedName,region);

        Branch savedBranch = branchRepository.save(branch);

        return branchMapper.toResponseBranchDTO(savedBranch);
    }

    public ResponseBranchDTO updateBranch(Long id, UpdateBranchDTO updateBranchDTO) {
        Objects.requireNonNull(id, "Id cannot be null");
        Objects.requireNonNull(updateBranchDTO, "Update branch DTO cannot be null");

        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Branch with id " + id + " does not exist"));

        branchMapper.updateBranchFromDTO(updateBranchDTO, branch);

        Branch savedBranch = branchRepository.save(branch);

        return branchMapper.toResponseBranchDTO(savedBranch);
    }

    public void delete(Long id) {
        Objects.requireNonNull(id,"Id cannot be null");

        if (!branchRepository.existsById(id)){
            throw new EntityNotFoundException("Branch with id " + id + " does not exist");
        }

        branchRepository.deleteById(id);
    }

    public boolean exists(Long id) {
        Objects.requireNonNull(id,"Id cannot be null");

        return branchRepository.existsById(id);
    }

    public boolean exists(String name) {
        Objects.requireNonNull(name, "Name cannot be null");

        return branchRepository.existsByName(name);
    }

    public List<ResponseBranchDTO> findAll() {
        List<Branch> branches = branchRepository.findAll();

        return branches.stream()
                .map(branchMapper::toResponseBranchDTO)
                .toList();
    }

    private Region getRegionReference(CreateBranchDTO createBranchDTO){
        if (!regionService.exists(createBranchDTO.regionId())){
            throw new EntityNotFoundException("Cannot find region by id " + createBranchDTO.regionId());
        }

        return entityManager.getReference(Region.class,createBranchDTO.regionId());
    }
}
