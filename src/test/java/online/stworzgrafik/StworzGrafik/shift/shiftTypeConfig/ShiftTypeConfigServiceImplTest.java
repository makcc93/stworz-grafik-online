package online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig;

import jakarta.persistence.EntityNotFoundException;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.shift.shiftTypeConfig.TestShiftTypeConfigBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShiftTypeConfigServiceImplTest {
    @InjectMocks
    private ShiftTypeConfigServiceImpl service;

    @Mock
    private ShiftTypeConfigRepository repository;

    @Test
    void findByCode_workingTest(){
        //given
        ShiftTypeConfig shiftTypeConfig = new TestShiftTypeConfigBuilder().withCode(ShiftCode.WORK).build();

        ShiftCode shiftCodeToFind = ShiftCode.WORK;
        when(repository.findByCode(shiftCodeToFind)).thenReturn(Optional.of(shiftTypeConfig));

        //when
        ShiftTypeConfig serviceResponse = service.findByCode(shiftCodeToFind);

        //then
        assertEquals(shiftCodeToFind,serviceResponse.getCode());
    }

    @Test
    void findByCode_codeNotFoundThrowsException(){
        //given
        ShiftTypeConfig shiftTypeConfig = new TestShiftTypeConfigBuilder().withCode(ShiftCode.WORK).build();

        ShiftCode shiftCodeToFind = ShiftCode.WORK;
        when(repository.findByCode(shiftCodeToFind)).thenReturn(Optional.empty());

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> service.findByCode(shiftCodeToFind));

        //then
        assertEquals("Cannot find shift type config by code " + shiftCodeToFind, exception.getMessage());
    }

    @Test
    void findByCode_codeIsNullThrowsException(){
        //given
        ShiftCode shiftCode = null;

        //when
        NullPointerException exception =
                assertThrows(NullPointerException.class, () -> service.findByCode(shiftCode));

        //then
        assertEquals("Shift code cannot be null", exception.getMessage());

        verify(repository,never()).findByCode(any());
    }

    //continue testing

}