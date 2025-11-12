package online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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

    @Test
    void getDefaultHours_workingTest(){
        //given
        BigDecimal tenHours = BigDecimal.valueOf(10L);
        ShiftCode code = ShiftCode.SICK_LEAVE;
        when(repository.getDefaultHours(code)).thenReturn(tenHours);

        //when
        BigDecimal serviceResponse = service.getDefaultHours(code);

        //then
        assertEquals(tenHours,serviceResponse);
    }

    @Test
    void getDefaultHours_codeIsNullThrowsException(){
        //given
        ShiftCode code = null;

        //when
        NullPointerException exception =
                assertThrows(NullPointerException.class, () -> service.getDefaultHours(code));

        //then
        assertEquals("Shift code cannot be null", exception.getMessage());
        verify(repository,never()).getDefaultHours(any());
    }

    @Test
    void countsAsWork_workingTest(){
        //given
        ShiftCode code = ShiftCode.FREE_DAY;
        boolean shouldBeFalse = false;
        when(repository.countsAsWork(code)).thenReturn(false);

        //when
        Boolean serviceResponse = service.countsAsWork(code);

        //then
        assertFalse(serviceResponse);
    }

    @Test
    void countsAsWork_codeIsNullThrowsException(){
        //given
        ShiftCode code = null;

        //when
        NullPointerException exception =
                assertThrows(NullPointerException.class, () -> service.countsAsWork(code));

        //then
        assertEquals("Shift code cannot be null", exception.getMessage());
    }
    //dodaj optional do repository i sprawdz warunki gdy zostanie zwrocony

}