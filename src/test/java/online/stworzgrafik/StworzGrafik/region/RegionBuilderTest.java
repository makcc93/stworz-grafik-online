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

    @Test
    void createRegion_nameValidatorTest(){
        //given
        String name = "  W e I r D name";
        String validatedName = "WEIRDNAME";

        //when
        Region region = new RegionBuilder().createRegion(name);

        //then
        assertEquals(validatedName,region.getName());
    }

    @Test
    void createRegion_nameArgumentIsNullThrowsException(){
        //given
        String name = null;

        //when
        NullPointerException exception = assertThrows(NullPointerException.class, () -> new RegionBuilder().createRegion(name));

        //then
        assertEquals("Name cannot be null", exception.getMessage());
    }
}