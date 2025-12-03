package online.stworzgrafik.StworzGrafik.draft;

import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;

public class TestDemandDraftBuilder {
    private Store store = new TestStoreBuilder().build();
    private Integer year = 2025;
    private Integer month = 1;
    private Integer day = 1;
    private int[] hourlyDemand = {0,0,0,0,0,0,0,0,4,8,9,9,9,9,11,11,11,11,11,11,7,0,0,0};

    public TestDemandDraftBuilder withStore (Store store){
        this.store = store;
        return this;
    }

    public TestDemandDraftBuilder withYear(Integer year){
        this.year = year;
        return this;
    }

    public TestDemandDraftBuilder withMonth(Integer month){
        this.month = month;
        return this;
    }

    public TestDemandDraftBuilder withDay(Integer day){
        this.day = day;
        return this;
    }

    public TestDemandDraftBuilder withHourlyDemand(int[] hourlyDemand){
        this.hourlyDemand = hourlyDemand;
        return this;
    }

    public DemandDraft build(){
        return new DemandDraftBuilder().createDemandDraft(
                store,
                year,
                month,
                day,
                hourlyDemand
        );
    }
}
