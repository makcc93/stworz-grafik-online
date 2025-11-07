package online.stworzgrafik.StworzGrafik.store;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface StoreRepository extends JpaRepository<Store, Long>, JpaSpecificationExecutor<Store> {
    boolean existsByNameAndStoreCode(String storeName,String storeCode);
    boolean existsByName(String name);
    boolean existsByStoreCode(String storeCode);
}
