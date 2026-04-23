package online.stworzgrafik.StworzGrafik.store.storeDetails;

import online.stworzgrafik.StworzGrafik.store.storeDetails.DTO.OptimalStaffingDTO;
import online.stworzgrafik.StworzGrafik.store.storeDetails.DTO.ResponseStoreDetailsDTO;

import java.time.LocalDateTime;

public class TestResponseStoreDetailsDTO {
    private Long id = 1L;
    private Long storeId = 54321L;
    private OptimalStaffingDTO optimalStafffingDTO = new TestOptimalStaffingDTO().build();
    private LocalDateTime updatedAt = LocalDateTime.of(2025,12,9,15,23);

    public ResponseStoreDetailsDTO build(){
        return new ResponseStoreDetailsDTO(
                id,
                storeId,
                optimalStafffingDTO,
                updatedAt,
                null,
                null,
                null
        );
    }
}
