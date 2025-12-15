package online.stworzgrafik.StworzGrafik.draft;

import online.stworzgrafik.StworzGrafik.store.Store;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
class DemandDraftBuilder {
    public DemandDraft createDemandDraft(
            Store store,
            LocalDate draftDate,
            int[] hourlyDemand) {
        return DemandDraft.builder()
                .store(store)
                .draftDate(draftDate)
                .hourlyDemand(hourlyDemand)
                .build();
    }
}
