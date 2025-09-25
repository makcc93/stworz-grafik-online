package online.stworzgrafik.StworzGrafik.store;

import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.TestBranchBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class StoreBuilderTest {

    @InjectMocks
    private StoreBuilder storeBuilder;

    @Mock
    private BranchBuilder branchBuilder;

    @Test
    void createStore_workingTest(){
        //given
        Branch branch = new TestBranchBuilder().build();

        //when
        Store store = storeBuilder.createStore(
                "NAME",
                "NA",
                "LOCATION",
                branch,
                RegionType.WSCHOD,
                LocalTime.of(10, 0),
                LocalTime.of(20, 0)
        );

        //then
        assertEquals("NAME",store.getName());
        assertEquals("NA",store.getStoreCode());
        assertEquals("LOCATION",store.getLocation());
        assertEquals(branch.getName(),store.getBranch().getName());
        assertEquals(RegionType.WSCHOD,store.getRegion());
        assertEquals(10,store.getOpenForClientsHour().getHour());
        assertEquals(20,store.getCloseForClientsHour().getHour());
    }

}