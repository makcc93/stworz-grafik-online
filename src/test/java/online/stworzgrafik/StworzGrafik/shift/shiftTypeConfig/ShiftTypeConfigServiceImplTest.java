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
    void getDefaultHours_workingTest(){
        //given
        BigDecimal tenHours = BigDecimal.valueOf(10L);
        ShiftCode code = ShiftCode.SICK_LEAVE;
        when(repository.getDefaultHours(code)).thenReturn(Optional.of(tenHours));

        //when
        BigDecimal serviceResponse = service.getDefaultHours(code);

        //then
        assertEquals(tenHours,serviceResponse);
    }

    @Test
    void getDefaultHours_cannotFindCodeReturnsZero(){
        //given
        ShiftCode code = ShiftCode.WORK;
        when(repository.getDefaultHours(code)).thenReturn(Optional.empty());

        //when
        BigDecimal serviceResponse = service.getDefaultHours(code);

        //then
        assertEquals(BigDecimal.ZERO, serviceResponse);
    }

    @Test
    void countsAsWork_workingTest(){
        //given
        ShiftCode code = ShiftCode.DAY_OFF;
        when(repository.countsAsWork(code)).thenReturn(false);

        //when
        Boolean serviceResponse = service.countsAsWork(code);

        //then
        assertFalse(serviceResponse);
    }
}