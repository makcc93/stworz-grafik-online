package online.stworzgrafik.StworzGrafik.region;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class RegionBuilderTest {

    @Test
    void createRegion_workingTest(){
        //given
        String name = "TEST";

        //when
        Region region = new RegionBuilder().createRegion(name);

        //then
        assertEquals(name, region.getName());
    }
}