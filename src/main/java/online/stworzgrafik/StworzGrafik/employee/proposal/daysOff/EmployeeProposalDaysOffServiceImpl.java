package online.stworzgrafik.StworzGrafik.employee.proposal.daysOff;

import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.draft.DTO.ResponseDemandDraftDTO;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.employee.EmployeeService;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.CreateEmployeeProposalDaysOffDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.ResponseEmployeeProposalDaysOffDTO;
import online.stworzgrafik.StworzGrafik.employee.proposal.daysOff.DTO.UpdateEmployeeProposalDaysOffDTO;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import online.stworzgrafik.StworzGrafik.store.StoreService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeProposalDaysOffServiceImpl implements EmployeeProposalDaysOffService{
    private final EmployeeProposalDaysOffRepository repository;
    private final EmployeeProposalDaysOffMapper mapper;
    private final EmployeeProposalDaysOffBuilder builder;
    private final UserAuthorizationService userAuthorizationService;
    private final StoreEntityService storeService;
    private final EmployeeEntityService employeeService;
    @Override
    public ResponseEmployeeProposalDaysOffDTO createEmployeeProposalDaysOff(Long storeId, Long employeeId, CreateEmployeeProposalDaysOffDTO dto) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }

        Store store = storeService.getEntityById(storeId);

        Employee employee = employeeService.getEntityById(employeeId);

        if (!employee.getStore().equals(store)){
            throw new AccessDeniedException("Employee with ID " + employee.getId() + " does not belong to store with ID " + store.getId());
        }

        if (repository.existsByStoreIdAndEmployeeIdAndYearAndMonth(storeId, employeeId, dto.year(), dto.month())){
            throw new EntityExistsException("Employee proposal days off in month " + dto.month() + " of  year " + dto.year() + " already exists");
        }

        EmployeeProposalDaysOff employeeProposalDaysOff = builder.createEmployeeProposalDaysOff(
                store,
                employee,
                dto.year(),
                dto.month(),
                dto.monthlyDaysOff()
        );

        return mapper.toResponseEmployeeProposalDaysOffDTO(employeeProposalDaysOff);
    }

    @Override
    public ResponseEmployeeProposalDaysOffDTO updateEmployeeProposalDaysOff(Long storeId, Long employeeId, UpdateEmployeeProposalDaysOffDTO dto) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }

        return null;
    }

    @Override
    public ResponseEmployeeProposalDaysOffDTO save(EmployeeProposalDaysOff employeeProposalDaysOff) {
      return null;
    }

    @Override
    public void delete(Long storeId, Long employeeId, Long employeeProposalDaysOffId) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }
    }

    @Override
    public ResponseDemandDraftDTO findById(Long storeId, Long employeeId, Long employeeProposalDaysOffId) {
        if (!userAuthorizationService.hasAccessToStore(storeId)){
            throw new AccessDeniedException("Access denied for store with id " + storeId);
        }

        return null;
    }

    @Override
    public boolean exists(Long employeeProposalDaysOffId) {
        return false;
    }
}
