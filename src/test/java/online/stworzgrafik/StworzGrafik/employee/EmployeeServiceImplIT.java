package online.stworzgrafik.StworzGrafik.employee;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchService;
import online.stworzgrafik.StworzGrafik.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.employee.position.PositionEntityService;
import online.stworzgrafik.StworzGrafik.employee.position.PositionService;
import online.stworzgrafik.StworzGrafik.employee.position.TestPositionBuilder;
import online.stworzgrafik.StworzGrafik.region.RegionService;
import online.stworzgrafik.StworzGrafik.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.security.UserAuthorizationService;
import online.stworzgrafik.StworzGrafik.store.StoreEntityService;
import online.stworzgrafik.StworzGrafik.store.StoreService;
import online.stworzgrafik.StworzGrafik.store.TestStoreBuilder;
import online.stworzgrafik.StworzGrafik.employee.DTO.CreateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.ResponseEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.UpdateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.position.Position;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.temporaryUser.UserContext;
import online.stworzgrafik.StworzGrafik.temporaryUser.UserRole;
import online.stworzgrafik.StworzGrafik.validator.NameValidatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
public class EmployeeServiceImplIT {
    @Autowired
    private EmployeeServiceImpl employeeServiceImpl;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private NameValidatorService nameValidatorService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private StoreEntityService storeEntityService;

    @Autowired
    private PositionService positionService;

    @Autowired
    private PositionEntityService positionEntityService;

    @Autowired
    private EmployeeBuilder employeeBuilder;

    @Autowired
    private BranchService branchService;

    @Autowired
    private RegionService regionService;

    @MockitoBean
    private UserAuthorizationService userAuthorizationService;

    private Region region;
    private Branch branch;
    private Store store;
    private Position position;

    private Long storeId;
    private Pageable pageable;

    @BeforeEach
    void setUpStoreAndPosition(){
        region = new TestRegionBuilder().build();
        regionService.save(region);

        branch = new TestBranchBuilder().withRegion(region).build();
        branchService.save(branch);

        store = new TestStoreBuilder().withBranch(branch).build();
        storeEntityService.saveEntity(store);

        position = new TestPositionBuilder().build();
        positionEntityService.saveEntity(position);

        storeId = store.getId();

        pageable = PageRequest.of(0,10);

        when(userAuthorizationService.hasAccessToStore(any())).thenReturn(true);
    }

    @Test
    void createEmployee_workingTest(){
        //given
        String firstName = "JOHN";
        String lastName = "SILVERHAND";
        Long sap = 2077L;

        CreateEmployeeDTO createEmployeeDTO = new TestCreateEmployeeDTO()
                .withFirstName(firstName)
                .withLastName(lastName)
                .withSap(sap)
                .withPositionId(position.getId())
                .build();


        //when
        ResponseEmployeeDTO serviceResponse = employeeServiceImpl.createEmployee(storeId,createEmployeeDTO);

        //then
        assertEquals(firstName,serviceResponse.firstName());
        assertEquals(lastName,serviceResponse.lastName());
        assertEquals(sap,serviceResponse.sap());
        assertEquals(storeId,serviceResponse.storeId());

        assertTrue(employeeServiceImpl.existsBySap(sap));
    }

    @Test
    void createEmployee_employeeWithThisSapAlreadyExistsThrowsException(){
        //given
        Employee employee = new TestEmployeeBuilder().withStore(store).withPosition(position).build();
        employeeServiceImpl.save(employee);

        Long employeeSap = employee.getSap();

        CreateEmployeeDTO createEmployeeDTO = new TestCreateEmployeeDTO().withSap(employeeSap).build();

        //when
        EntityExistsException exception =
                assertThrows(EntityExistsException.class, () -> employeeServiceImpl.createEmployee(storeId,createEmployeeDTO));

        //then
        assertEquals("Employee with sap " + employeeSap + " already exists", exception.getMessage());
        assertTrue(employeeServiceImpl.existsBySap(employeeSap));
    }

    @Test
    void createEmployee_namesValidationThrowsException(){
        //given
        String invalidFirstName = "WRONG FIRST NAME!@#$%^&*()";
        CreateEmployeeDTO invalidFirstNameDTO = new TestCreateEmployeeDTO().withFirstName(invalidFirstName).build();

        String invalidLastName = "WRONG LAST NAME!@$%^&*()";
        CreateEmployeeDTO invalidLastNameDTO = new TestCreateEmployeeDTO().withLastName(invalidLastName).build();

        //when
        ValidationException exceptionFistName =
                assertThrows(ValidationException.class, () -> employeeServiceImpl.createEmployee(storeId,invalidFirstNameDTO));
        ValidationException exceptionLastName =
                assertThrows(ValidationException.class, () -> employeeServiceImpl.createEmployee(storeId,invalidLastNameDTO));

        //then
        assertEquals("Name cannot contain illegal chars",exceptionFistName.getMessage());
        assertEquals(exceptionFistName.getMessage(), exceptionLastName.getMessage());
    }

    @Test
    void createEmployee_sapNumberLengthIsIncorrectThrowsException(){
        //given
        Long randomPositionId = 12345L;
        CreateEmployeeDTO withoutPositionDTO = new TestCreateEmployeeDTO()
                .withPositionId(randomPositionId)
                .build();

        Long randomStoreId = 54321L;
        CreateEmployeeDTO withoutStoreDTO = new TestCreateEmployeeDTO()
                .withPositionId(position.getId())
                .build();

        //when
        EntityNotFoundException exceptionForPosition =
                assertThrows(EntityNotFoundException.class, () -> employeeServiceImpl.createEmployee(storeId,withoutPositionDTO));

        EntityNotFoundException exceptionForStore =
                assertThrows(EntityNotFoundException.class, () -> employeeServiceImpl.createEmployee(randomStoreId,withoutStoreDTO));

        //then
        assertEquals("Cannot find position by id " + randomPositionId, exceptionForPosition.getMessage());
        assertEquals("Cannot find store by id " + randomStoreId, exceptionForStore.getMessage());
    }

    @Test
    void updateEmployee_workingTest(){
        //given
        String originalFirstName = "ORIGINAL FIRST NAME";
        String originalLastName = "ORIGINAL LAST NAME";

        Employee employee = new TestEmployeeBuilder()
                .withFirstName(originalFirstName)
                .withLastName(originalLastName)
                .withStore(store)
                .withPosition(position)
                .build();
        employeeServiceImpl.save(employee);

        Long employeeId = employee.getId();

        String updatedFirstName = "UPDATED FIRST NAME";
        String updatedLastName = "UPDATED LAST NAME";
        boolean updatedOpenCloseStore = false;
        boolean updatedManager = false;
        UpdateEmployeeDTO updateEmployeeDTO = new TestUpdateEmployeeDTO()
                .withFirstName(updatedFirstName)
                .withLastName(updatedLastName)
                .withCanOpenCloseStore(updatedOpenCloseStore)
                .withManager(updatedManager)
                .build();

        //when
        ResponseEmployeeDTO serviceResponse = employeeServiceImpl.updateEmployee(storeId, employeeId, updateEmployeeDTO);

        //then
        assertEquals(updatedFirstName,serviceResponse.firstName());
        assertEquals(updatedLastName,serviceResponse.lastName());
        assertEquals(updatedOpenCloseStore,serviceResponse.canOpenCloseStore());
        assertEquals(updatedManager,serviceResponse.manager());

        assertEquals(employee.getId(),serviceResponse.id());
        assertEquals(employee.getSap(),serviceResponse.sap());
        assertEquals(employee.getPosition().getId(),serviceResponse.positionId());
        assertEquals(employee.getStore().getId(),serviceResponse.storeId());
        assertEquals(employee.isCanOperateCheckout(),serviceResponse.canOperateCheckout());
        assertEquals(employee.isCanOperateCredit(),serviceResponse.canOperateCredit());
        assertEquals(employee.isSeller(),serviceResponse.seller());
    }

    @Test
    void updateEmployee_entityDoesNotExistThrowsException(){
        //given
        Long randomEmployeeId = 12345L;
        UpdateEmployeeDTO updateEmployeeDTO = new TestUpdateEmployeeDTO().build();

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> employeeServiceImpl.updateEmployee(storeId,randomEmployeeId, updateEmployeeDTO));

        //then
        assertEquals("Cannot find employee by id " + randomEmployeeId, exception.getMessage());
    }

    @Test
    void updateEmployee_invalidNamesIsDtoThrowsException(){
        //given
        Employee employee = new TestEmployeeBuilder().withStore(store).withPosition(position).build();
        employeeServiceImpl.save(employee);

        String invalidFirstName = "!@#$%^&*()";
        UpdateEmployeeDTO updateEmployeeDTO = new TestUpdateEmployeeDTO().withFirstName(invalidFirstName).build();

        //when
        ValidationException exception =
                assertThrows(ValidationException.class, () -> employeeServiceImpl.updateEmployee(storeId, employee.getId(), updateEmployeeDTO));

        //then
        assertEquals("Name cannot contain illegal chars", exception.getMessage());
    }

    @Test
    void deleteEmployee_workingTest(){
        //given
        String stay = "STAY";
        String delete = "DELETE";

        Employee employeeToStay = new TestEmployeeBuilder().withStore(store).withPosition(position).withFirstName(stay).build();
        employeeServiceImpl.save(employeeToStay);

        Employee employeeToDelete = new TestEmployeeBuilder().withStore(store).withPosition(position).withFirstName(delete).build();
        employeeServiceImpl.save(employeeToDelete);

        Long employeeToStayId = employeeToStay.getId();
        Long employeeToDeleteId = employeeToDelete.getId();

        //when
        employeeServiceImpl.deleteEmployee(storeId,employeeToDeleteId);

        //then
        assertTrue(employeeServiceImpl.existsById(employeeToStayId));

        assertFalse(employeeServiceImpl.existsById(employeeToDeleteId));
    }

    @Test
    void deleteEmployee_employeeWithIdDoesNotExistThrowsException(){
        //given
        Long randomEmployeeId = 1234L;

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> employeeServiceImpl.deleteEmployee(storeId,randomEmployeeId));

        //then
        assertEquals("Cannot find employee by id " + randomEmployeeId, exception.getMessage());
    }

    @Test
    void deleteEmployee_cannotFindEmployeeThrowsException(){
        //given
        Long randomEmployeeId = 54321L;

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> employeeServiceImpl.deleteEmployee(storeId, randomEmployeeId));

        //then
        assertEquals("Cannot find employee by id " + randomEmployeeId,exception.getMessage());
    }

    @Test
    void deleteEmployee_employeeDoesNotBelongToStoreThrowsException(){
        //given
        Employee employee = new TestEmployeeBuilder().withStore(store).withPosition(position).build();
        employeeServiceImpl.save(employee);

        Long employeeId = employee.getId();

        Long notExistingStoreId = 123456L;

        //when
        AccessDeniedException exception =
                assertThrows(AccessDeniedException.class, () -> employeeServiceImpl.deleteEmployee(notExistingStoreId, employeeId));

        //then
        assertEquals("Employee does not belong to this store", exception.getMessage());
    }

    @Test
    void findAll_workingTest(){
        //given
        Employee first = new TestEmployeeBuilder().withStore(store).withPosition(position).withFirstName("FIRST").build();
        employeeServiceImpl.save(first);
        Employee second = new TestEmployeeBuilder().withStore(store).withPosition(position).withFirstName("SECOND").build();
        employeeServiceImpl.save(second);
        Employee third = new TestEmployeeBuilder().withStore(store).withPosition(position).withFirstName("THIRD").build();
        employeeServiceImpl.save(third);

        ResponseEmployeeDTO firstResponseEmployeeDTO = new TestResponseEmployeeDTO().fromEmployee(first).build();
        ResponseEmployeeDTO secondResponseEmployeeDTO = new TestResponseEmployeeDTO().fromEmployee(second).build();
        ResponseEmployeeDTO thirdResponseEmployeeDTO = new TestResponseEmployeeDTO().fromEmployee(third).build();
        List<ResponseEmployeeDTO> employeeDTOs = List.of(firstResponseEmployeeDTO,secondResponseEmployeeDTO,thirdResponseEmployeeDTO);

        //when
        Page<ResponseEmployeeDTO> serviceResponse = employeeServiceImpl.findAll(pageable);

        //then
        assertEquals(3,serviceResponse.getContent().size());
        assertTrue(serviceResponse.getContent().containsAll(employeeDTOs));
    }

    @Test
    void findAll_emptyListDoesNotThrowException(){
        //given

        //when
        Page<ResponseEmployeeDTO> serviceResponse = employeeServiceImpl.findAll(pageable);

        //then
        assertEquals(0, serviceResponse.getContent().size());
        assertDoesNotThrow(() -> employeeServiceImpl.findAll(pageable));
    }

    @Test
    void findById_workingTest(){
        //given
        Employee employee = new TestEmployeeBuilder().withStore(store).withPosition(position).build();
        employeeServiceImpl.save(employee);
        Long employeeId = employee.getId();

//        when(userContext.getManagedStoreIds()).thenReturn(List.of(store.getId()));
//        when(userContext.getUserRole()).thenReturn(UserRole.STORE_MANAGER);
//        when(userContext.getUserStoreId()).thenReturn(store.getId());

        //when
        ResponseEmployeeDTO serviceResponse = employeeServiceImpl.findById(storeId, employeeId);

        //then
        assertEquals(employee.getFirstName(), serviceResponse.firstName());
        assertEquals(employee.getLastName(),serviceResponse.lastName());
        assertEquals(employee.getSap(),serviceResponse.sap());
        assertEquals(employee.getStore().getId(),serviceResponse.storeId());
        assertEquals(employee.getPosition().getId(),serviceResponse.positionId());
        assertEquals(employee.isCanOperateCheckout(),serviceResponse.canOperateCheckout());
        assertEquals(employee.isCanOperateCredit(),serviceResponse.canOperateCredit());
        assertEquals(employee.isCanOpenCloseStore(),serviceResponse.canOpenCloseStore());
        assertEquals(employee.isSeller(),serviceResponse.seller());
        assertEquals(employee.isManager(),serviceResponse.manager());
        assertEquals(employee.getCreatedAt(),serviceResponse.createdAt());
        assertEquals(employee.getUpdatedAt(),serviceResponse.updatedAt());
    }

    @Test
    void existsById_workingTest(){
        //given
        Employee employee = new TestEmployeeBuilder().withStore(store).withPosition(position).build();
        employeeServiceImpl.save(employee);

        Long id = employee.getId();
        Long randomId = 12345L;

        //when
        boolean serviceResponse = employeeServiceImpl.existsById(id);
        boolean shouldNotExist = employeeServiceImpl.existsById(randomId);

        //then
        assertTrue(serviceResponse);
        assertFalse(shouldNotExist);
    }

    @Test
    void existsBySap_workingTest(){
        //given
        Long sap = 1230123L;
        Employee employee = new TestEmployeeBuilder().withStore(store).withPosition(position).withSap(sap).build();
        employeeServiceImpl.save(employee);

        Long randomSap = 11111111L;

        //when
        boolean serviceResponse = employeeServiceImpl.existsBySap(sap);
        boolean shouldBeFalse = employeeServiceImpl.existsBySap(randomSap);

        //then
        assertTrue(serviceResponse);
        assertFalse(shouldBeFalse);
    }

    @Test
    void existsByLastName_workingTest(){
        //given
        String lastName = "TEST-LAST-NAME";
        Employee employee = new TestEmployeeBuilder().withStore(store).withPosition(position).withLastName(lastName).build();
        employeeServiceImpl.save(employee);

        String randomLastName = "RANDOM";

        //when
        boolean serviceResponse = employeeServiceImpl.existsByLastName(lastName);
        boolean shouldBeFalse = employeeServiceImpl.existsByLastName(randomLastName);

        //then
        assertTrue(serviceResponse);
        assertFalse(shouldBeFalse);
    }

    private Store getDefaultSavedStore(){
        Region region = new TestRegionBuilder().build();
        regionService.save(region);

        Branch branch = new TestBranchBuilder().withRegion(region).build();
        branchService.save(branch);

        Store store = new TestStoreBuilder().withBranch(branch).build();
        return storeEntityService.saveEntity(store);
    }

    private Position getDefaultSavedPosition(){
        Position position = new TestPositionBuilder().build();
        return positionEntityService.saveEntity(position);
    }
}
