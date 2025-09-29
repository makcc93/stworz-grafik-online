package online.stworzgrafik.StworzGrafik.region;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RegionRepository extends JpaRepository<Region,Long>, JpaSpecificationExecutor<Region> {
    boolean existsByName(String name);
}
