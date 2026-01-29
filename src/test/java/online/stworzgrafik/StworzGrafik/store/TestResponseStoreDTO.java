package online.stworzgrafik.StworzGrafik.store;

import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.storeDetails.DTO.ResponseStoreDetailsDTO;
import online.stworzgrafik.StworzGrafik.store.storeDetails.TestResponseStoreDetailsDTO;
import org.apache.coyote.Response;

import java.time.LocalDateTime;

public class TestResponseStoreDTO {
    private Long id = 1L;
    private String name = "TESTCREATESTORE";
    private String storeCode = "CC";
    private String location = "TESTCREATELOCATION";
    private Branch branch = new TestBranchBuilder().build();
    private LocalDateTime createdAt = LocalDateTime.now();
    private boolean enable = true;
    private Long storeManagerId = 50L;
    private String storeManagerFullName = "STEVE JOBS";
    private ResponseStoreDetailsDTO details = new TestResponseStoreDetailsDTO().build();

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

    public TestResponseStoreDTO withStoreManagerFullName(String storeManagerFullName){
        this.storeManagerFullName = storeManagerFullName;
        return this;
    }

    public TestResponseStoreDTO withDetails(ResponseStoreDetailsDTO details){
        this.details = details;
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
                storeManagerId,
                storeManagerFullName,
                details
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
                store.getStoreManagerId(),
                store.getStoreManagerId().toString(),
                new TestResponseStoreDetailsDTO().build()
        );
    }
}
