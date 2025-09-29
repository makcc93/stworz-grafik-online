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

    @Mock
    private BranchBuilder branchBuilder;

    @Mock
    private BranchRepository branchRepository;

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
                LocalTime.of(10, 0),
                LocalTime.of(20, 0)
        );

        //then
        assertEquals("NAME",store.getName());
        assertEquals("NA",store.getStoreCode());
        assertEquals("LOCATION",store.getLocation());
        assertEquals(branch.getName(),store.getBranch().getName());
        assertEquals(10,store.getOpenForClientsHour().getHour());
        assertEquals(20,store.getCloseForClientsHour().getHour());
    }

    @Test
    void create_endHourIsBeforeStartHourThrowsException(){
        //given
        LocalTime startHour = LocalTime.of(20,0);
        LocalTime endHour = LocalTime.of(8,0);

        Branch branch = new TestBranchBuilder().build();

        //when
        assertThrows(IllegalArgumentException.class,() ->
                new StoreBuilder().createStore(
                "NAME",
                "NE",
                "LOCATION",
                branch,
                startHour,
                endHour
        ),"Close hour cannot be before open hour");

        //then
    }

}