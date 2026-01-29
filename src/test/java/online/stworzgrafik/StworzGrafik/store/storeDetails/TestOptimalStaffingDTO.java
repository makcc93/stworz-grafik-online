package online.stworzgrafik.StworzGrafik.store.storeDetails;

import online.stworzgrafik.StworzGrafik.store.storeDetails.DTO.OptimalStaffingDTO;

public class TestOptimalStaffingDTO {
    private Integer storeManagers = 1;
    private Integer salesManagers = 2;
    private Integer sellers = 10;
    private Integer cashiers = 1;
    private Integer storemen = 1;
    private Integer pok = 0;
    private Integer total = storeManagers + salesManagers + cashiers + storemen + pok;

    public TestOptimalStaffingDTO withStoreManagers(Integer storeManagers) {
        this.storeManagers = storeManagers;
        return this;
    }

    public TestOptimalStaffingDTO withSalesManagers(Integer salesManagers) {
        this.salesManagers = salesManagers;
        return this;
    }

    public TestOptimalStaffingDTO withSellers(Integer sellers) {
        this.sellers = sellers;
        return this;
    }

    public TestOptimalStaffingDTO withCashiers(Integer cashiers) {
        this.cashiers = cashiers;
        return this;
    }

    public TestOptimalStaffingDTO withStoremen(Integer storemen) {
        this.storemen = storemen;
        return this;
    }

    public TestOptimalStaffingDTO withPok(Integer pok) {
        this.pok = pok;
        return this;
    }

    public OptimalStaffingDTO build(){
        return new OptimalStaffingDTO(
                storeManagers,
                salesManagers,
                sellers,
                cashiers,
                storemen,
                pok,
                total
        );
    }
}
