package online.stworzgrafik.StworzGrafik.store.storeDetails;

import online.stworzgrafik.StworzGrafik.store.DTO.UpdateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.storeDetails.DTO.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
interface StoreDetailsMapper {
    @Mapping(source = "store.id", target = "storeId")
    ResponseStoreDetailsDTO toDto(StoreDetails storeDetails);

    StoreHoursDTO toDto(StoreHours hours);

    OptimalStaffingDTO toDto(OptimalStaffing staffing);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "store", source = "store")
    StoreDetails toEntity(CreateStoreDetailsDTO dto, Store store);

    StoreHours toEntity(StoreHoursDTO dto);

    OptimalStaffing toEntity(OptimalStaffingDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "store", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateStoreDetailsFromDTO(UpdateStoreDetailsDTO updateStoreDetailsDTO, @MappingTarget StoreDetails storeDetails);
}
