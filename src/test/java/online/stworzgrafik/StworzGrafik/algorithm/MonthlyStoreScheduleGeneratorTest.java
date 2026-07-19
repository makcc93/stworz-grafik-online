package online.stworzgrafik.StworzGrafik.algorithm;

import online.stworzgrafik.StworzGrafik.algorithm.analyzer.rest.RestAnalyzer;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.rest.WeeklyRequirementRest;
import online.stworzgrafik.StworzGrafik.algorithm.analyzer.shift.ScheduleAnalyzer;
import online.stworzgrafik.StworzGrafik.algorithm.deliveryCover.WarehousemanScheduleGenerator;
import online.stworzgrafik.StworzGrafik.algorithm.preparation.DaysOffApplier;
import online.stworzgrafik.StworzGrafik.algorithm.preparation.DelegationApplier;
import online.stworzgrafik.StworzGrafik.algorithm.preparation.ProposalShiftApplier;
import online.stworzgrafik.StworzGrafik.algorithm.preparation.VacationApplier;
import online.stworzgrafik.StworzGrafik.algorithm.rolesMatcher.CheckoutMatcher;
import online.stworzgrafik.StworzGrafik.algorithm.rolesMatcher.CreditMatcher;
import online.stworzgrafik.StworzGrafik.algorithm.rolesMatcher.OpenCloseMatcher;
import online.stworzgrafik.StworzGrafik.algorithm.specialEmployees.SpecialEmployeesShiftMatcher;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.TestEmployeeBuilder;
import online.stworzgrafik.StworzGrafik.fileExport.ExcelExport;
import online.stworzgrafik.StworzGrafik.fileExport.PdfExport;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.schedule.Schedule;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthlyStoreScheduleGeneratorTest {
    @InjectMocks
    private MonthlyStoreScheduleGenerator monthlyStoreScheduleGenerator;

    @Mock
    private ScheduleGeneratorContext context;

    @Mock
    private ScheduleGeneratorContextFactory contextFactory;

    @Mock
    private VacationApplier vacationApplier;

    @Mock
    private DelegationApplier delegationApplier;

    @Mock
    private DaysOffApplier daysOffApplier;

    @Mock
    private ProposalShiftApplier proposalShiftApplier;

    @Mock
    private WeeklyRequirementRest weeklyRequirementRest;

    @Mock
    private WarehousemanScheduleGenerator warehousemanScheduleGenerator;

    @Mock
    private DailyShiftGeneratorAlgorithm dailyShiftGeneratorAlgorithm;

    @Mock
    private EmployeeToShiftMatcher employeeToShiftMatcher;

    @Mock
    private ExcelExport excelExport;

    @Mock
    private SpecialEmployeesShiftMatcher specialEmployeesShiftMatcher;

    @Mock
    private PdfExport pdfExport;

    @Mock
    private ScheduleAnalyzer scheduleAnalyzer;

    @Mock
    private RestAnalyzer restAnalyzer;

    @Mock
    private EmptyDaysMatcher emptyDaysMatcher;

    @Mock
    private CreditMatcher creditMatcher;

    @Mock
    private CheckoutMatcher checkoutMatcher;

    @Mock
    private OpenCloseMatcher openCloseMatcher;

    @Mock
    private ScheduleDatabaseSaver scheduleDatabaseSaver;

    @Mock
    private Schedule schedule;

    private final int year = 2026;
    private final int month = 3;
    private Long scheduleId = 1L;

    private Region region;
    private Branch branch;
    private Store store;

    @BeforeEach
    void setup(){
        region = new TestRegionBuilder().build();
        branch = new TestBranchBuilder().withRegion(region).build();
        store = new TestStoreBuilder().withBranch(branch).build();

        when(context.getStoreNotSpecialActiveEmployees()).thenReturn(getEmployees());

        when(contextFactory.create(any(),any(),any())).thenReturn(context);
    }

    @Test
    void generateMonthlySchedule_workingTest() throws IOException {
        // given
        when(context.getSchedule()).thenReturn(schedule);
        when(context.getSchedule().getId()).thenReturn(scheduleId);
        // when
        monthlyStoreScheduleGenerator.generateMonthlySchedule(store.getId(),year,month);

        // then
        verify(contextFactory).create(store.getId(), year, month);
        verify(vacationApplier).applyVacationsToSchedule(context);
        verify(delegationApplier).applyDelegationToSchedule(context);
        verify(daysOffApplier).applyDaysOffToSchedule(context);
        verify(proposalShiftApplier).applyProposalShiftsToSchedule(context);
        verify(weeklyRequirementRest).proceed(context);
        verify(warehousemanScheduleGenerator).generate(context);
        verify(dailyShiftGeneratorAlgorithm).generateShiftsToDays(context);
        verify(employeeToShiftMatcher).matchEmployeeToShift(context);
        verify(dailyShiftGeneratorAlgorithm).modifyShiftsHours(context);
        verify(emptyDaysMatcher).completeEmptyDaysWithDayOffShift(context);
        verify(scheduleDatabaseSaver).saveScheduleToDatabase(store.getId(), context);
    }

    private List<Employee> getEmployees(){
        return List.of(
                new TestEmployeeBuilder().withFirstName("Damian").withLastName("Mrozicki").withSap(10000001L).withCanOpenCloseStore(true).withStore(store).build(),
                new TestEmployeeBuilder().withFirstName("Monika").withLastName("Baran").withSap(10000002L).withCanOpenCloseStore(true).withStore(store).build(),
                new TestEmployeeBuilder().withFirstName("Mateusz").withLastName("Kruk").withSap(10000003L).withCanOpenCloseStore(true).withStore(store).build(),

                new TestEmployeeBuilder().withFirstName("Filip").withLastName("Kamiński").withSap(10000004L).withCanOpenCloseStore(true).withCanOperateCheckout(true).withCanOperateCredit(true).withCanOperateDelivery(true).withStore(store).build(),
                new TestEmployeeBuilder().withFirstName("Martyna").withLastName("Nowicka").withSap(10000005L).withCanOpenCloseStore(true).withCanOperateCheckout(true).withCanOperateCredit(true).withCanOperateDelivery(true).withStore(store).build(),

                new TestEmployeeBuilder().withFirstName("Wojciech").withLastName("Pietruszka").withSap(10000006L).withCanOperateCheckout(true).withCanOperateCredit(true).withCanOperateDelivery(true).withStore(store).build(),
                new TestEmployeeBuilder().withFirstName("Michał").withLastName("Woch").withSap(10000007L).withCanOperateCheckout(true).withCanOperateCredit(true).withCanOperateDelivery(true).withStore(store).build(),
                new TestEmployeeBuilder().withFirstName("Tomasz").withLastName("Zając").withSap(10000008L).withCanOperateCheckout(true).withCanOperateCredit(true).withCanOperateDelivery(true).withStore(store).build(),
                new TestEmployeeBuilder().withFirstName("Agata").withLastName("Warmińska").withSap(10000009L).withCanOperateCheckout(true).withCanOperateCredit(true).withCanOperateDelivery(true).withStore(store).build(),

                new TestEmployeeBuilder().withFirstName("Michał").withLastName("Kozik").withSap(10000010L).withCanOperateCheckout(true).withCanOperateDelivery(true).withStore(store).build(),
                new TestEmployeeBuilder().withFirstName("Marcin").withLastName("Przepiórka").withSap(10000011L).withCanOperateCheckout(true).withCanOperateDelivery(true).withStore(store).build(),
                new TestEmployeeBuilder().withFirstName("Marcin").withLastName("Wojtas").withSap(10000012L).withCanOperateCheckout(true).withCanOperateDelivery(true).withStore(store).build(),

                new TestEmployeeBuilder().withFirstName("Olga").withLastName("Beznazwiska").withSap(10000013L).withStore(store).build(),

                new TestEmployeeBuilder().withFirstName("Karolina").withLastName("Nakonieczna").withSap(10000014L).withCashier(true).withCanOperateCheckout(true).withStore(store).build(),

                new TestEmployeeBuilder().withFirstName("Emil").withLastName("Miazek").withSap(10000015L).withWarehouseman(true).withStore(store).build()
        );
    }
}