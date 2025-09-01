package online.stworzgrafik.StworzGrafik.store;

import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.StoreNameAndCodeDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StoreMapper {

    ResponseStoreDTO toResponseStoreDto(Store store);

    StoreNameAndCodeDTO toStoreNameAndCode(CreateStoreDTO createStoreDTO);

    Store toEntity(CreateStoreDTO createStoreDTO);

    Store toEntity(StoreNameAndCodeDTO storeNameAndCodeDTO);
}
