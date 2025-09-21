package online.stworzgrafik.StworzGrafik.store;

import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.StoreNameAndCodeDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.UpdateStoreDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface StoreMapper {

    @Mapping(source = "branch.id", target = "branchId")
    @Mapping(source = "branch.name",target = "branchName")
    ResponseStoreDTO toResponseStoreDto(Store store);

    StoreNameAndCodeDTO toStoreNameAndCodeDTO(CreateStoreDTO createStoreDTO);

    @Mapping(source = "branchId", target = "branch.id")
    Store toEntity(CreateStoreDTO createStoreDTO);

    Store toEntity(StoreNameAndCodeDTO storeNameAndCodeDTO);

    @Mapping(target = "branch",ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateStoreFromDTO(UpdateStoreDTO updateStoreDTO, @MappingTarget Store store);
}
