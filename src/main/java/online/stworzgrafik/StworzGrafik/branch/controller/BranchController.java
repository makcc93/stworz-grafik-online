package online.stworzgrafik.StworzGrafik.branch.controller;

import jakarta.validation.Valid;
import online.stworzgrafik.StworzGrafik.branch.BranchService;
import online.stworzgrafik.StworzGrafik.branch.DTO.NameBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.ResponseBranchDTO;
import online.stworzgrafik.StworzGrafik.branch.DTO.UpdateBranchDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
public class BranchController {
    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @GetMapping
    public ResponseEntity<List<ResponseBranchDTO>> findAll(){
        return ResponseEntity.ok().body(branchService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseBranchDTO> findById(@PathVariable Long id){
        return ResponseEntity.ok().body(branchService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ResponseBranchDTO> createBranch(@RequestBody @Valid NameBranchDTO nameBranchDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(branchService.createBranch(nameBranchDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteBranchById(@PathVariable Long id){
        branchService.delete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ResponseBranchDTO> updateBranch(@PathVariable Long id, @RequestBody @Valid UpdateBranchDTO updateBranchDTO){
        return ResponseEntity.ok().body(branchService.updateBranch(id,updateBranchDTO));
    }
}
