package online.stworzgrafik.StworzGrafik.converter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class IntArrayJsonConverterTest {
    @InjectMocks
    private IntArrayJsonConverter converter;

    @Test
    void convertToDatabaseColumn_workingTest(){
        //given
        int[] demand = {0,0,0,0,0};
        String expectedJson = "[0,0,0,0,0]";

        //when
        String converterResponse = converter.convertToDatabaseColumn(demand);

        //then
        assertEquals(expectedJson,converterResponse);
    }

    @Test
    void convertToDatabaseColumn_emptyArrayDoesNotThrowException(){
        //given
        int[] demand = {};
        String expectedJson = "[]";
        //when
        String converterResponse = converter.convertToDatabaseColumn(demand);

        //then
        assertEquals("[]", converterResponse);
    }

    @Test
    void convertToDatabaseColumn_nullIsAcceptable(){
        //given
        int[] demand = null;
        String expectedJson = null;
        //when
        String converterResponse = converter.convertToDatabaseColumn(demand);

        //then
        assertEquals(expectedJson, converterResponse);
    }

    @Test
    void convertToEntityAttribute_workingTest(){
        //given
        String json = "[1,1,1]";
        int[] expectedArray = {1,1,1};

        //when
        int[] converterResponse = converter.convertToEntityAttribute(json);

        //then
        assertArrayEquals(expectedArray,converterResponse);
    }

    @Test
    void convertToEntityAttribute_nullIsAcceptable(){
        //given
        String json = null;
        int[] expectedArray = null;

        //when
        int[] ints = converter.convertToEntityAttribute(json);

        //then
        assertEquals(expectedArray,ints);
    }
}