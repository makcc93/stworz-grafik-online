package online.stworzgrafik.StworzGrafik.employee.position;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.position.TestCreatePositionDTO;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.position.TestPositionBuilder;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.CreatePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.ResponsePositionDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class PositionServiceImplIT {

    @Autowired
    private PositionService service;

    @Autowired
    private PositionMapper mapper;

    @Autowired
    private PositionBuilder builder;

    @Autowired
    private PositionRepository repository;

    @Test
    void findAll_workingTest(){
        //given
        Position position1 = new TestPositionBuilder().withName("FIRST").build();
        Position position2 = new TestPositionBuilder().withName("SECOND").build();
        Position position3 = new TestPositionBuilder().withName("THIRD").build();
        List<ResponsePositionDTO> dtos = List.of(position1,position2,position3).stream()
                .map(mapper::toResponsePositionDTO)
                .toList();

        repository.saveAll(List.of(position1,position2,position3));

        //when
        List<ResponsePositionDTO> responseDTOS = service.findAll();

        //then
        assertTrue(responseDTOS.contains(mapper.toResponsePositionDTO(position1)));
        assertTrue(responseDTOS.contains(mapper.toResponsePositionDTO(position2)));
        assertTrue(responseDTOS.contains(mapper.toResponsePositionDTO(position3)));
        assertEquals(3, responseDTOS.size());
    }

    @Test
    void findById_workingTest(){
        //given
        Position position1 = new TestPositionBuilder().withName("FIRST").build();
        Position position2 = new TestPositionBuilder().withName("SECOND").build();
        repository.saveAll(List.of(position1,position2));

        //when
        ResponsePositionDTO serviceResponse = service.findById(position2.getId());

        //then
        assertEquals(position2.getId(),serviceResponse.id());
        assertEquals(position2.getName(),serviceResponse.name());
        assertEquals(position2.getDescription(),serviceResponse.description());
    }

    @Test
    void findById_entityNotExistThrowsException(){
        //given
        Long randomId = 123L;

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> service.findById(randomId));

        //then
        assertEquals("Cannot find position by id " + randomId, exception.getMessage());
    }

    @Test
    void createPosition_workingTest(){
        //given
        String name = "NEW NAME";
        String description = "NEW DESCRIPTION";

        CreatePositionDTO createPositionDTO = new TestCreatePositionDTO().withName(name).withDescription(description).build();

        //when
        ResponsePositionDTO serviceResponse = service.createPosition(createPositionDTO);

        //then
        assertEquals(name,serviceResponse.name());
        assertEquals(description,serviceResponse.description());
        assertTrue(repository.existsByName(name));
    }
}
