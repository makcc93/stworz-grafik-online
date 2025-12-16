package online.stworzgrafik.StworzGrafik.draft;

import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;

import java.time.LocalDate;

public class TestDemandDraftBuilder {
    private Store store = new TestStoreBuilder().build();
    private LocalDate draftDate = LocalDate.of(2025,12,12);
    private int[] hourlyDemand = {0,0,0,0,0,0,0,0,4,8,9,9,9,9,11,11,11,11,11,11,7,0,0,0};

    public TestDemandDraftBuilder withStore (Store store){
        this.store = store;
        return this;
    }

    public TestDemandDraftBuilder withDraftDate(LocalDate draftDate){
        this.draftDate = draftDate;
        return this;
    }

    public TestDemandDraftBuilder withHourlyDemand(int[] hourlyDemand){
        this.hourlyDemand = hourlyDemand;
        return this;
    }

    public DemandDraft build(){
        return new DemandDraftBuilder().createDemandDraft(
                store,
                draftDate,
                hourlyDemand
        );
    }
}
