package online.stworzgrafik.StworzGrafik.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser,Long> {
    @Query("SELECT u FROM AppUser u " +
            "LEFT JOIN FETCH u.store " +
            "LEFT JOIN FETCH u.branch " +
            "LEFT JOIN FETCH u.region " +
            "WHERE u.login = :login")
    Optional<AppUser> findByLogin(@Param("login") String login);

    boolean existsByLogin(String login);

    @Query("SELECT s.id FROM Store s WHERE s.branch.region.id = :regionId")
    List<Long> findStoreIdsByRegionId(@Param("regionId") Long regionId);

    @Query("SELECT s.id FROM Store s WHERE s.branch.id = :branchId")
    List<Long> findStoreIdsByBranchId(@Param("branchId") Long branchId);
}
