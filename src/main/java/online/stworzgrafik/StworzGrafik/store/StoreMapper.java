package online.stworzgrafik.StworzGrafik.store;

import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.StoreNameAndCodeDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.UpdateStoreDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface StoreMapper {

    ResponseStoreDTO toResponseStoreDto(Store store);

    StoreNameAndCodeDTO toStoreNameAndCodeDTO(CreateStoreDTO createStoreDTO);

    Store toEntity(CreateStoreDTO createStoreDTO);

    Store toEntity(StoreNameAndCodeDTO storeNameAndCodeDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateStoreFromDTO(UpdateStoreDTO updateStoreDTO, @MappingTarget Store store);
}
