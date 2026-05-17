package online.stworzgrafik.StworzGrafik.employee.workNorm;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpecialWorkNormRepository extends JpaRepository<SpecialWorkNorm, Long> {
    List<SpecialWorkNorm> findAllByActiveTrue();
    boolean existsByName(String name);
}
