package online.stworzgrafik.StworzGrafik.employee.proposal.shifts;

import jakarta.persistence.PrePersist;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.employee.TestEmployeeBuilder;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmployeeProposalShiftsServiceImplTest {
    @InjectMocks
    private EmployeeProposalShiftsServiceImpl service;

    @Mock
    private EmployeeProposalShiftsBuilder builder;

    @Mock
    private EmployeeProposalShiftsMapper mapper;

    @Mock
    private EmployeeProposalShiftsRepository repository;

    @Mock
    private UserAuthorizationService userAuthorizationService;

    @Mock
    private StoreEntityService storeService;

    @Mock
    private EmployeeEntityService employeeService;

    private Long storeId = 123L;
    private Long employeeId = 321L;
    private Long employeeProposalShiftId = 50L;
    private Store store;
    private Employee employee;

    @PrePersist
    void setup(){
        store = new TestStoreBuilder().build();
        employee = new TestEmployeeBuilder().withStore(store).build();
    }

    @Test
    void createEmployeeProposalShift_workingTest(){
        //stopped here
    }



}