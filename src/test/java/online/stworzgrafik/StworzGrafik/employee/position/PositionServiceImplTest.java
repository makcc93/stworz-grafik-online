package online.stworzgrafik.StworzGrafik.employee.position;

import online.stworzgrafik.StworzGrafik.dataBuilderForTests.position.TestPositionBuilder;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.ResponsePositionDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PositionServiceImplTest {
    @InjectMocks
    private PositionServiceImpl positionService;

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private PositionMapper positionMapper;

    @Test
    void findAll_workingTest(){
        //given
        Position position1 = new TestPositionBuilder().withName("FIRST").build();
        Position position2 = new TestPositionBuilder().withName("SECOND").build();
        Position position3 = new TestPositionBuilder().withName("THIRD").build();

        when(positionRepository.save(position1)).thenReturn(position1);
        when(positionRepository.save(position2)).thenReturn(position2);
        when(positionRepository.save(position3)).thenReturn(position3);

        //when

        //then
    }

}