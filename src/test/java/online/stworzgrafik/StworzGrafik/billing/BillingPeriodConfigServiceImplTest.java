package online.stworzgrafik.StworzGrafik.billing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillingPeriodConfigServiceImplTest {
    @InjectMocks
    private BillingPeriodConfigServiceImpl service;

    @Mock
    private BillingPeriodConfigMapper mapper;

    @Mock
    private BillingPeriodConfigRepository repository;

    @Test
    void create_workingTest(){
        //given
        when(repository.findByStartMonth());
        //when

        //then
    }
}