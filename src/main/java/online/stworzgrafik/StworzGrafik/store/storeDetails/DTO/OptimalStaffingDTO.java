package online.stworzgrafik.StworzGrafik.store.storeDetails.DTO;

public record OptimalStaffingDTO(
        Integer storeManagers,
        Integer salesManagers,
        Integer sellers,
        Integer cashiers,
        Integer storemen,
        Integer pok,
        Integer total
) {
    public OptimalStaffingDTO {
        total = (storeManagers != null ? storeManagers : 0) +
                (salesManagers != null ? salesManagers : 0) +
                (sellers != null ? sellers : 0) +
                (cashiers != null ? cashiers : 0) +
                (storemen != null ? storemen : 0) +
                (pok != null ? pok : 0);
    }
}