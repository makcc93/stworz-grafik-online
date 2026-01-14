package online.stworzgrafik.StworzGrafik.branch.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.branch.BranchService;
import online.stworzgrafik.StworzGrafik.branch.DTO.CreateBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.ResponseBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.UpdateBranchDTO;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Transactional
@RequestMapping("/api")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
class BranchController {
    private final BranchService branchService;
    private final UserAuthorizationService userAuthorizationService;


    @GetMapping("/branches")
    public ResponseEntity<List<ResponseBranchDTO>> findAll(){
        return ResponseEntity.ok().body(branchService.findAll());
    }

    @GetMapping("/branches/{id}")
    public ResponseEntity<ResponseBranchDTO> findById(@PathVariable Long id){
        return ResponseEntity.ok().body(branchService.findById(id));
    }

    @PostMapping("/branches")
    public ResponseEntity<ResponseBranchDTO> createBranch(@RequestBody @Valid CreateBranchDTO createBranchDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(branchService.createBranch(createBranchDTO));
    }

    @DeleteMapping("/branches/{id}")
    public ResponseEntity<HttpStatus> deleteBranchById(@PathVariable Long id){
        branchService.delete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/branches/{id}")
    public ResponseEntity<ResponseBranchDTO> updateBranch(@PathVariable Long id, @RequestBody @Valid UpdateBranchDTO updateBranchDTO){
        return ResponseEntity.ok().body(branchService.updateBranch(id,updateBranchDTO));
    }
}
