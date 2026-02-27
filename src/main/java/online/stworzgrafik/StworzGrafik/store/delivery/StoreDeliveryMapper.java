package online.stworzgrafik.StworzGrafik.store.delivery;

import online.stworzgrafik.StworzGrafik.store.delivery.DTO.ResponseStoreDeliveryDTO;
import online.stworzgrafik.StworzGrafik.store.delivery.DTO.UpdateStoreDeliveryDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
interface StoreDeliveryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "storeId", source = "store.id")
    @Mapping(target = "primaryEmployeeId", source = "employee.id")
    ResponseStoreDeliveryDTO toDTO(StoreDelivery storeDelivery);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "store.id", source = "storeId")
    @Mapping(target = "employee.id", source = "primaryEmployeeId")
    StoreDelivery toEntity(ResponseStoreDeliveryDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "store", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(UpdateStoreDeliveryDTO dto, StoreDelivery storeDelivery);
}
