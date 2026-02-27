package online.stworzgrafik.StworzGrafik.store.storeDetails;

import online.stworzgrafik.StworzGrafik.store.storeDetails.DTO.OptimalStaffingDTO;
import online.stworzgrafik.StworzGrafik.store.storeDetails.DTO.ResponseStoreDetailsDTO;
import online.stworzgrafik.StworzGrafik.store.storeDetails.DTO.StoreHoursDTO;

import java.time.LocalDateTime;

public class TestResponseStoreDetailsDTO {
    private Long id = 1L;
    private Long storeId = 54321L;
    private StoreHoursDTO storeHoursDTO = new TestStoreHoursDTO().build();
    private OptimalStaffingDTO optimalStafffingDTO = new TestOptimalStaffingDTO().build();
    private LocalDateTime updatedAt = LocalDateTime.of(2025,12,9,15,23);

    public ResponseStoreDetailsDTO build(){
        return new ResponseStoreDetailsDTO(
                id,
                storeId,
                storeHoursDTO,
                optimalStafffingDTO,
                updatedAt,
                null,
                null,
                null
        );
    }
}
