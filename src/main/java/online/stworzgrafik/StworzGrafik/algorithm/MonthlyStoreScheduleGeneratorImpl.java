package online.stworzgrafik.StworzGrafik.algorithm;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.draft.DemandDraftEntityService;
import online.stworzgrafik.StworzGrafik.employee.DTO.EmployeeSpecificationDTO;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Transactional()
class MonthlyStoreScheduleGeneratorImpl implements MonthlyStoreScheduleGenerator{
    private final UserAuthorizationService userAuthorizationService;
    private final StoreEntityService storeEntityService;
    private final EmployeeEntityService employeeEntityService;
    private final DemandDraftEntityService demandDraftEntityService;


    private final Store store = storeEntityService.getEntityById(userAuthorizationService.getUserStoreId());
    private final List<Employee> storeEmployees = employeeEntityService.findAllStoreActiveEmployees(store.getId());
    private final Map<Integer, int[]> storeMonthlyDemandDrafts = demandDraftEntityService.



    @Override
    public List<String> generateMonthlySchedule(Long storeId, Integer year, Integer month, Pageable pageable) {
        return null;
    }
}
