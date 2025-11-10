package online.stworzgrafik.StworzGrafik.branch;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.branch.DTO.CreateBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.ResponseBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.UpdateBranchDTO;
import online.stworzgrafik.StworzGrafik.region.RegionService;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.RegionEntityService;
import online.stworzgrafik.StworzGrafik.validator.NameValidatorService;
import online.stworzgrafik.StworzGrafik.validator.ObjectType;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
class BranchServiceImpl implements BranchService, BranchEntityService{
    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;
    private final BranchBuilder branchBuilder;
    private final NameValidatorService nameValidatorService;
    private final RegionService regionService;
    private final RegionEntityService regionEntityService;

    @Override
    public ResponseBranchDTO findById(@Valid Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Branch with id " + id + " does not exist"));

        return branchMapper.toResponseBranchDTO(branch);
    }

    public List<ResponseBranchDTO> findAll() {
        return branchRepository.findAll().stream()
                .map(branchMapper::toResponseBranchDTO)
                .toList();
    }

    @Override
    public ResponseBranchDTO createBranch(@Valid CreateBranchDTO createBranchDTO) {
        if (branchRepository.existsByName(createBranchDTO.name())){
            throw new EntityExistsException("Branch with name " + createBranchDTO.name() + " already exist");
        }

        String validatedName = nameValidatorService.validate(createBranchDTO.name(), ObjectType.BRANCH);

        Region region = regionEntityService.getEntityById(createBranchDTO.regionId());

        Branch branch = branchBuilder.createBranch(validatedName,region);

        Branch savedBranch = branchRepository.save(branch);

        return branchMapper.toResponseBranchDTO(savedBranch);
    }

    @Override
    public ResponseBranchDTO updateBranch(@Valid Long id, @Valid UpdateBranchDTO updateBranchDTO) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Branch with id " + id + " does not exist"));

        branchMapper.updateBranchFromDTO(updateBranchDTO, branch);

        Branch savedBranch = branchRepository.save(branch);

        return branchMapper.toResponseBranchDTO(savedBranch);
    }

    @Override
    public ResponseBranchDTO save(Branch branch) {
        Branch savedBranch = branchRepository.save(branch);

        return branchMapper.toResponseBranchDTO(savedBranch);
    }

    @Override
    public void delete(@Valid Long id) {
        if (!branchRepository.existsById(id)){
            throw new EntityNotFoundException("Branch with id " + id + " does not exist");
        }

        branchRepository.deleteById(id);
    }
    @Override
    public boolean exists(@Valid Long id) {
        return branchRepository.existsById(id);
    }

    @Override
    public boolean exists(@Valid String name) {
        return branchRepository.existsByName(name);
    }

    @Override
    public Branch saveEntity(@Valid Branch branch) {
        return branchRepository.save(branch);
    }

    @Override
    public Branch getEntityById(@Valid Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find branch by id " + id));
    }
}
