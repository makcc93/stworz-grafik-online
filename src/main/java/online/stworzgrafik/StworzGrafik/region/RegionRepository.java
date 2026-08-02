package online.stworzgrafik.StworzGrafik.region;

import online.stworzgrafik.StworzGrafik.region.DTO.ResponseRegionDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

interface RegionRepository extends JpaRepository<Region,Long>, JpaSpecificationExecutor<Region> {
    boolean existsByName(String name);
    Optional<Region> findByName(String name);
}
