package online.stworzgrafik.StworzGrafik.store.storeDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class OptimalStaffing {

    @Column(name = "optimal_store_managers")
    private Integer storeManagers;

    @Column(name = "optimal_sales_managers")
    private Integer salesManagers;

    @Column(name = "optimal_sellers")
    private Integer sellers;

    @Column(name = "optimal_cashiers")
    private Integer cashiers;

    @Column(name = "optimal_storemen")
    private Integer storemen;

    @Column(name = "optimal_pok")
    private Integer pok;

    public static OptimalStaffing createDefault() {
        return OptimalStaffing.builder()
                .storeManagers(1)
                .salesManagers(2)
                .sellers(10)
                .cashiers(3)
                .storemen(2)
                .pok(0)
                .build();
    }

    public int getTotalStaff() {
        return (storeManagers != null ? storeManagers : 0) +
                (salesManagers != null ? salesManagers : 0) +
                (sellers != null ? sellers : 0) +
                (cashiers != null ? cashiers : 0) +
                (storemen != null ? storemen : 0) +
                (pok != null ? pok : 0);
    }
}