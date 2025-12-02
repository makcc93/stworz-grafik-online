package online.stworzgrafik.StworzGrafik.draft;

import online.stworzgrafik.StworzGrafik.store.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

interface DemandDraftRepository extends JpaRepository<DemandDraft,Long>, JpaSpecificationExecutor<DemandDraft> {
    boolean existsByStoreAndYeahAndMonthAndDay(Store store, Integer year, Integer month, Integer day);
}
