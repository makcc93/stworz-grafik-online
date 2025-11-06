package online.stworzgrafik.StworzGrafik.demand;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DemandDraftRepository extends JpaRepository<DemandDraft,Long>, JpaSpecificationExecutor<DemandDraft> {
}
