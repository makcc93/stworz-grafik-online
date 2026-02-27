package online.stworzgrafik.StworzGrafik.store.delivery;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StoreDeliveryRepository extends JpaRepository<StoreDelivery, Long>, JpaSpecificationExecutor<StoreDelivery> {
}
