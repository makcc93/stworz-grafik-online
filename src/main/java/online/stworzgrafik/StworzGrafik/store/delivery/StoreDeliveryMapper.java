package online.stworzgrafik.StworzGrafik.store.delivery;

import online.stworzgrafik.StworzGrafik.store.delivery.DTO.ResponseStoreDeliveryDTO;
import online.stworzgrafik.StworzGrafik.store.delivery.DTO.UpdateStoreDeliveryDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface StoreDeliveryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "storeId", source = "store.id")
    @Mapping(target = "primaryEmployeeId", source = "primaryEmployee.id")
    ResponseStoreDeliveryDTO toDTO(StoreDelivery storeDelivery);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "store.id", source = "storeId")
    @Mapping(target = "primaryEmployee.id", source = "primaryEmployeeId")
    StoreDelivery toEntity(ResponseStoreDeliveryDTO dto);

//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "store", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(UpdateStoreDeliveryDTO dto, @MappingTarget StoreDelivery storeDelivery);
}
