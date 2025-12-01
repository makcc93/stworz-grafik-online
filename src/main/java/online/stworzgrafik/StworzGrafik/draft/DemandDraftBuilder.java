package online.stworzgrafik.StworzGrafik.draft;

import online.stworzgrafik.StworzGrafik.store.Store;
import org.springframework.stereotype.Component;

@Component
class DemandDraftBuilder {
    public DemandDraft createDemandDraft(
            Store store,
            Integer year,
            Integer month,
            Integer day,
            int[] hourlyDemand) {
        return DemandDraft.builder()
                .store(store)
                .year(year)
                .month(month)
                .day(day)
                .hourlyDemand(hourlyDemand)
                .build();
    }
}
