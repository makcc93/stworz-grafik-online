package online.stworzgrafik.StworzGrafik.store.storeDetails;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

interface StoreDetailsRepository extends JpaRepository<StoreDetails,Long>, JpaSpecificationExecutor<StoreDetails> {
    Optional<StoreDetails> findByStoreId(Long storeId);

    boolean existsByStoreId(Long storeId);
}
