package online.stworzgrafik.StworzGrafik.store;

import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseDetailStoreDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StoreMapper {

    ResponseDetailStoreDTO toDetailStoreDto(Store store);

    Store toEntity(CreateStoreDTO createStoreDTO);
}
