package online.stworzgrafik.StworzGrafik.region.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.region.DTO.CreateRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.RegionSpecificationDTO;
import online.stworzgrafik.StworzGrafik.region.RegionService;
import online.stworzgrafik.StworzGrafik.region.DTO.ResponseRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.UpdateRegionDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
class RegionController {
    private final RegionService regionService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/regions")
    public ResponseEntity<List<ResponseRegionDTO>> getAll(){

        return ResponseEntity.ok(regionService.findAll());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/regions/{id}")
    public ResponseEntity<ResponseRegionDTO> getById(@PathVariable Long id){
        return ResponseEntity.ok(regionService.findById(id));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/regions")
    public ResponseEntity<ResponseRegionDTO> createRegion(@RequestBody @Valid CreateRegionDTO createRegionDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(regionService.createRegion(createRegionDTO));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/regions/{id}")
    public ResponseEntity<HttpStatus> deleteRegion(@PathVariable Long id){
        regionService.delete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PatchMapping("/regions/{id}")
    public ResponseEntity<ResponseRegionDTO> updateRegion(@PathVariable Long id, @RequestBody @Valid UpdateRegionDTO updateRegionDTO){
        return ResponseEntity.ok().body(regionService.updateRegion(id,updateRegionDTO));
    }
}
