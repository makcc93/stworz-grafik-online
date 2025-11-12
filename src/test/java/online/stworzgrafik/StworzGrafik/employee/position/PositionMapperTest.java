package online.stworzgrafik.StworzGrafik.employee.position;

import online.stworzgrafik.StworzGrafik.employee.position.DTO.ResponsePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.UpdatePositionDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PositionMapperTest {
    private final PositionMapper positionMapper = new PositionMapperImpl();

    @Test
    void toResponsePositionDTO_wokingTest(){
        //given
        String name = "POSITION NAME";
        Position position = new TestPositionBuilder().withName(name).build();

        //when
        ResponsePositionDTO responsePositionDTO = positionMapper.toResponsePositionDTO(position);

        //then
        assertEquals(name, responsePositionDTO.name());
    }

    @Test
    void updatePosition_workingTest(){
        //given
        String oldName = "OLD NAME";
        String oldDescription = "OLD DESCRIPTION";
        Position position = new TestPositionBuilder().withName(oldName).withDescription(oldDescription).build();


        String newName = "NEW NAME";
        String nullDescritpion = null;
        UpdatePositionDTO updatePositionDTO = new TestUpdatePositionDTO().withName(newName).withDescription(nullDescritpion).build();

        //when
        positionMapper.updatePosition(updatePositionDTO,position);

        //then
        assertEquals(newName,position.getName());
        assertEquals(oldDescription,position.getDescription());
    }

    @Test
    void updatePosition_nullableDescriptionUpdateTest(){
        //given
        String oldName = "OLD NAME";
        String oldDescription = null;
        Position position = new TestPositionBuilder().withName(oldName).withDescription(oldDescription).build();

        String newName = "NEW NAME";
        String newDescription = "NEW DESCRIPTION";
        UpdatePositionDTO updatePositionDTO = new TestUpdatePositionDTO().withName(newName).withDescription(newDescription).build();

        //when
        positionMapper.updatePosition(updatePositionDTO,position);

        //then
        assertEquals(newName,position.getName());
        assertEquals(newDescription,position.getDescription());
    }

}