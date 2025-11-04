package online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ShiftTypeConfigServiceImplTest {
    @InjectMocks
    private ShiftTypeConfigServiceImpl service;

    @Mock
    private ShiftTypeConfigRepository repository;

    @Mock
    private ShiftTypeConfig shiftTypeConfig;

    //zastanawiam sie czy to robic, czy tu potrzebny service i serviceImpl a potem testy
    //czyli 1. czy config ma miec service, testy, itd

    //2. rob dalej demand_draft :D
    @Test
    void findByCode_workingTest(){
        //given
        ShiftCode shiftCode = ShiftCode.WORK;

        Mockito.when(repository.findByCode(shiftCode)).thenReturn(Optional.of(shiftTypeConfig));

        //when
        ShiftTypeConfig serviceResponse = service.findByCode(shiftCode);

        //then

    }

}