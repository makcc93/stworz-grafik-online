package online.stworzgrafik.StworzGrafik.temporaryUser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser,Long> {
    Optional<AppUser> findByLogin(String login);
    boolean existsByLogin(String login);

    @Query("SELECT s.id FROM Store s WHERE s.branch.region.id = :regionId")
    List<Long> findStoreIdsByRegionId(@Param("regionId") Long regionId);

    @Query("SELECT s.id FROM Store s WHERE s.branch.id = :branchId")
    List<Long> findStoreIdsByBranchId(@Param("branchId") Long branchId);
}
