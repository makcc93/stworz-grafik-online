package online.stworzgrafik.StworzGrafik.employee.position;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.position.TestCreatePositionDTO;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.position.TestPositionBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.position.TestUpdatePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.CreatePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.ResponsePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.UpdatePositionDTO;
import online.stworzgrafik.StworzGrafik.validator.NameValidatorService;
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

    @Autowired
    private NameValidatorService nameValidatorService;

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

    @Test
    void createPosition_entityAlreadyExistsThrowsException(){
        //given
        String name = "NAME";
        Position position = new TestPositionBuilder().withName(name).build();
        repository.save(position);

        CreatePositionDTO createPositionDTO = new TestCreatePositionDTO().withName(name).build();

        //when
        EntityExistsException exception =
                assertThrows(EntityExistsException.class, () -> service.createPosition(createPositionDTO));
        //then
        assertEquals("Position with name " + name + " already exists", exception.getMessage());
        assertTrue(repository.existsByName(name));
    }

    @Test
    void createPosition_nameToUpperCaseValidationTest(){
        //given
        String givenName = "manager";
        String expectedName = "MANAGER";

        CreatePositionDTO createPositionDTO = new TestCreatePositionDTO().withName(givenName).build();

        //when
        ResponsePositionDTO serviceResponse = service.createPosition(createPositionDTO);

        //then
        assertEquals(expectedName,serviceResponse.name());
    }

    @Test
    void createPosition_illegalCharsInNameThrowsException(){
        //given
        String name = "!@#$%^&*(){}";

        CreatePositionDTO createPositionDTO = new TestCreatePositionDTO().withName(name).build();

        //when
       ValidationException exception =
                assertThrows(ValidationException.class, () -> service.createPosition(createPositionDTO));

        //then
        assertEquals("Name cannot contain illegal chars", exception.getMessage());
    }

    @Test
    void updatePosition_workingTest(){
        //given
        String oldName = "OLD NAME";
        String oldDescription = "OLD DESCRIPTION";
        Position position = new TestPositionBuilder().withName(oldName).withDescription(oldDescription).build();
        repository.save(position);

        String newName = "NEW NAME";
        UpdatePositionDTO updatePositionDTO = new TestUpdatePositionDTO().withName(newName).withDescription(null).build();

        //when
        ResponsePositionDTO onlyNameUpdated = service.updatePosition(position.getId(), updatePositionDTO);

        //then
        assertEquals(position.getId(),onlyNameUpdated.id());
        assertEquals(newName,onlyNameUpdated.name());
        assertEquals(oldDescription,onlyNameUpdated.description());
    }

    @Test
    void updatePosition_entityDoesNotExistThrowsException(){
        //given
        Long randomId = 12345L;
        UpdatePositionDTO updatePositionDTO = new TestUpdatePositionDTO().build();

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> service.updatePosition(randomId, updatePositionDTO));

        //then
        assertEquals("Cannot find position by id " + randomId, exception.getMessage());
        assertFalse(repository.existsById(randomId));
    }

    @Test
    void deletePosition_workingTest(){
        //given
        Position position1 = new TestPositionBuilder().withName("FIRST").build();
        Position positionToDelete = new TestPositionBuilder().withName("SECOND").build();
        Position position2 = new TestPositionBuilder().withName("THIRD").build();
        repository.saveAll(List.of(position1,positionToDelete,position2));

        //when
        service.deletePosition(positionToDelete.getId());

        //then
        assertFalse(repository.existsById(positionToDelete.getId()));
        assertTrue(repository.existsById(position1.getId()));
        assertTrue(repository.existsById(position2.getId()));
    }

    @Test
    void deletePosition_entityDoesNotExistThrowsException(){
        //given
        Long randomId = 12345L;

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> service.deletePosition(randomId));

        //then
        assertEquals("Position with id " + randomId + " does not exist", exception.getMessage());
        assertFalse(repository.existsById(randomId));
    }

    @Test
    void existsById_workingTest(){
        //given
        String name = "NAME";
        Position position = new TestPositionBuilder().withName(name).build();
        repository.save(position);

        Long savedPositionId = position.getId();

        //when
        boolean response = service.exists(savedPositionId);

        //then
        assertTrue(response);
    }

    @Test
    void existsByName_workingTest(){
        //given
        String name = "NAME";
        Position position = new TestPositionBuilder().withName(name).build();
        repository.save(position);

        //when
        boolean response = service.exists(name);

        //then
        assertTrue(response);
    }
}
