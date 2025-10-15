package online.stworzgrafik.StworzGrafik.store;

import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchBuilder;
import online.stworzgrafik.StworzGrafik.branch.BranchRepository;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.branch.TestBranchBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class StoreBuilderTest {

    @InjectMocks
    private StoreBuilder storeBuilder;

    @Test
    void createStore_workingTest(){
        //given
        Branch branch = new TestBranchBuilder().build();

        //when
        Store store = storeBuilder.createStore(
                "NAME",
                "NA",
                "LOCATION",
                branch
        );

        //then
        assertEquals("NAME",store.getName());
        assertEquals("NA",store.getStoreCode());
        assertEquals("LOCATION",store.getLocation());
        assertEquals(branch.getName(),store.getBranch().getName());
    }
}