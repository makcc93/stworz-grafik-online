package online.stworzgrafik.StworzGrafik.dataBuilderForTests.store;

import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.Store;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class TestResponseStoreDTO {
    private Long id = 6988L;
    private String name = "TESTCREATESTORE";
    private String storeCode = "CC";
    private String location = "TESTCREATELOCATION";
    private Branch branch = new TestBranchBuilder().build();
    private LocalDateTime createdAt = LocalDateTime.now();
    private boolean enable = true;
    private Long storeManagerId = 50L;

    public TestResponseStoreDTO withId(Long id){
        this.id = id;
        return this;
    }

    public TestResponseStoreDTO withName(String name){
        this.name = name;
        return this;
    }

    public TestResponseStoreDTO withStoreCode(String storeCode){
        this.storeCode = storeCode;
        return this;
    }

    public TestResponseStoreDTO withLocation(String location){
        this.location = location;
        return this;
    }

    public TestResponseStoreDTO withBranch(Branch branch){
        this.branch = branch;
        return this;
    }

    public TestResponseStoreDTO withCreatedAt(LocalDateTime createdAt){
        this.createdAt = createdAt;
        return this;
    }

    public TestResponseStoreDTO withIsEnable(boolean enable){
        this.enable = enable;
        return this;
    }

    public TestResponseStoreDTO withStoreManagerId(Long storeManagerId){
        this.storeManagerId = storeManagerId;
        return this;
    }

    public ResponseStoreDTO build(){
        return new ResponseStoreDTO(
                id,
                name,
                storeCode,
                location,
                branch.getId(),
                branch.getName(),
                createdAt,
                enable,
                storeManagerId
        );
    }

    public ResponseStoreDTO buildFromEntity(Store store){
        return new ResponseStoreDTO(
                store.getId(),
                store.getName(),
                store.getStoreCode(),
                store.getLocation(),
                store.getBranch().getId(),
                store.getBranch().getName(),
                store.getCreatedAt(),
                store.isEnable(),
                store.getStoreManagerId()
        );
    }
}
