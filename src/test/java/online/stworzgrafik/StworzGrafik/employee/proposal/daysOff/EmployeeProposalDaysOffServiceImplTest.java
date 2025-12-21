package online.stworzgrafik.StworzGrafik.employee.proposal.daysOff;

import jakarta.persistence.PrePersist;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.employee.TestEmployeeBuilder;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.CreateEmployeeProposalDaysOffDTO;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeProposalDaysOffServiceImplTest {
    @InjectMocks
    private EmployeeProposalDaysOffServiceImpl service;

    @Mock
    private EmployeeProposalDaysOffBuilder builder;

    @Mock
    private EmployeeProposalDaysOffMapper mapper;

    @Mock
    private EmployeeProposalDaysOffRepository repository;

    @Mock
    private UserAuthorizationService userAuthorizationService;

    @Mock
    private StoreEntityService storeService;

    @Mock
    private EmployeeEntityService employeeService;

    private Long storeId = 1L;
    private Long employeeId = 9L;
    private Store store;
    private Employee employee;
    @PrePersist
    void setup(){
        store = new TestStoreBuilder().build();
        employee = new TestEmployeeBuilder().withStore(store).build();
    }

    @Test
    void createEmployeeProposalDaysOff_workingTest(){
        //given
        Integer year = 2025;
        Integer month = 12;
        int[] monthlyDaysOff = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1};

        when(userAuthorizationService.hasAccessToStore(storeId)).thenReturn(true);

        when(storeService.getEntityById(storeId)).thenReturn(store);

        when(employeeService.getEntityById(employeeId)).thenReturn(employee);

        when(repository.existsByStoreIdAndEmployeeIdAndYearAndMonth(storeId,employeeId,year,month)).thenReturn(false);

        CreateEmployeeProposalDaysOffDTO createEmployeeProposalDaysOffDTO =
                new TestCreateEmployeeProposalDaysOffDTO()
                        .withYear(year)
                        .withMonth(month)
                        .withMonthlyDaysOff(monthlyDaysOff)
                        .build();

        when(builder.createEmployeeProposalDaysOff(store,employee,year,month,monthlyDaysOff)).thenReturn()

        //when

        //then
    }

}