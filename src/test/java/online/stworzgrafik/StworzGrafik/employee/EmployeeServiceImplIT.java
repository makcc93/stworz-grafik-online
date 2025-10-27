package online.stworzgrafik.StworzGrafik.employee;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import online.stworzgrafik.StworzGrafik.branch.Branch;
import online.stworzgrafik.StworzGrafik.branch.BranchRepository;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.branch.TestBranchBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.employee.TestCreateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.employee.TestEmployeeBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.employee.TestResponseEmployeeDTO;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.employee.TestUpdateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.position.TestPositionBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.region.TestRegionBuilder;
import online.stworzgrafik.StworzGrafik.dataBuilderForTests.store.TestStoreBuilder;
import online.stworzgrafik.StworzGrafik.employee.DTO.CreateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.ResponseEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.UpdateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.position.Position;
import online.stworzgrafik.StworzGrafik.employee.position.PositionRepository;
import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.RegionRepository;
import online.stworzgrafik.StworzGrafik.store.Store;
import online.stworzgrafik.StworzGrafik.store.StoreRepository;
import online.stworzgrafik.StworzGrafik.validator.NameValidatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class EmployeeServiceImplIT {
    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private NameValidatorService nameValidatorService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private EmployeeBuilder employeeBuilder;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Test
    void createEmployee_workingTest(){
        //given
        String firstName = "JOHN";
        String lastName = "SILVERHAND";
        Long sap = 2077L;
        Store defaultSavedStore = getDefaultSavedStore();
        Position defaultSavedPosition = getDefaultSavedPosition();

        CreateEmployeeDTO createEmployeeDTO = new TestCreateEmployeeDTO()
                .withFirstName(firstName)
                .withLastName(lastName)
                .withSap(sap)
                .withStoreId(defaultSavedStore.getId())
                .withPositionId(defaultSavedPosition.getId())
                .build();


        //when
        ResponseEmployeeDTO serviceResponse = employeeService.createEmployee(createEmployeeDTO);

        //then
        assertEquals(firstName,serviceResponse.firstName());
        assertEquals(lastName,serviceResponse.lastName());
        assertEquals(sap,serviceResponse.sap());

        assertTrue(employeeRepository.existsBySap(sap));
    }

    @Test
    void createEmployee_employeeWithThisSapAlreadyExistsThrowsException(){
        //given
        Store store = getDefaultSavedStore();
        Position position = getDefaultSavedPosition();

        Employee employee = new TestEmployeeBuilder().withStore(store).withPosition(position).build();
        employeeRepository.save(employee);

        Long employeeSap = employee.getSap();

        CreateEmployeeDTO createEmployeeDTO = new TestCreateEmployeeDTO().withSap(employeeSap).build();

        //when
        EntityExistsException exception =
                assertThrows(EntityExistsException.class, () -> employeeService.createEmployee(createEmployeeDTO));

        //then
        assertEquals("Employee with sap " + employeeSap + " already exists", exception.getMessage());
        assertTrue(employeeRepository.existsBySap(employeeSap));
    }

    @Test
    void createEmployee_namesValidationThrowsException(){
        //given
        String invalidFirstName = "WRONG FIRST NAME!@#$%^&*()";
        CreateEmployeeDTO invalidFirstNameDTO = new TestCreateEmployeeDTO().withFirstName(invalidFirstName).build();

        String invalidLastName = "WRONG LAST NAME!@$%^&*()";
        CreateEmployeeDTO invalidLastNameDTO = new TestCreateEmployeeDTO().withLastName(invalidLastName).build();

        //when
        ValidationException exceptionFistName = assertThrows(ValidationException.class, () -> employeeService.createEmployee(invalidFirstNameDTO));
        ValidationException exceptionLastName = assertThrows(ValidationException.class, () -> employeeService.createEmployee(invalidLastNameDTO));

        //then
        assertEquals("Name cannot contain illegal chars",exceptionFistName.getMessage());
        assertEquals(exceptionFistName.getMessage(), exceptionLastName.getMessage());
    }

    @Test
    void createEmployee_sapNumberLengthIsIncorrectThrowsException(){
        //given
        Store store = getDefaultSavedStore();
        Position position = getDefaultSavedPosition();

        Long randomPositionId = 12345L;
        CreateEmployeeDTO withoutPositionDTO = new TestCreateEmployeeDTO()
                .withPositionId(randomPositionId)
                .withStoreId(store.getId())
                .build();

        Long randomStoreId = 54321L;
        CreateEmployeeDTO withoutStoreDTO = new TestCreateEmployeeDTO()
                .withStoreId(randomStoreId)
                .withPositionId(position.getId())
                .build();

        //when
        EntityNotFoundException exceptionForPosition =
                assertThrows(EntityNotFoundException.class, () -> employeeService.createEmployee(withoutPositionDTO));

        EntityNotFoundException exceptionForStore =
                assertThrows(EntityNotFoundException.class, () -> employeeService.createEmployee(withoutStoreDTO));

        //then
        assertEquals("Cannot find position by id " + randomPositionId, exceptionForPosition.getMessage());
        assertEquals("Cannot find store by id " + randomStoreId, exceptionForStore.getMessage());
    }

    @Test
    void updateEmployee_workingTest(){
        //given
        String originalFirstName = "ORIGINAL FIRST NAME";
        String originalLastName = "ORIGINAL LAST NAME";

        Store store = getDefaultSavedStore();
        Position position = getDefaultSavedPosition();

        Employee employee = new TestEmployeeBuilder()
                .withFirstName(originalFirstName)
                .withLastName(originalLastName)
                .withStore(store)
                .withPosition(position)
                .build();
        employeeRepository.save(employee);

        Long id = employee.getId();

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
        ResponseEmployeeDTO serviceResponse = employeeService.updateEmployee(id, updateEmployeeDTO);

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
        Long randomId = 12345L;
        UpdateEmployeeDTO updateEmployeeDTO = new TestUpdateEmployeeDTO().build();

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> employeeService.updateEmployee(randomId, updateEmployeeDTO));

        //then
        assertEquals("Cannot find employee by id " + randomId, exception.getMessage());
    }

    @Test
    void updateEmployee_invalidNamesIsDtoThrowsException(){
        //given
        Store store = getDefaultSavedStore();
        Position position = getDefaultSavedPosition();
        Employee employee = new TestEmployeeBuilder().withStore(store).withPosition(position).build();
        employeeRepository.save(employee);

        String invalidFirstName = "!@#$%^&*()";
        UpdateEmployeeDTO updateEmployeeDTO = new TestUpdateEmployeeDTO().withFirstName(invalidFirstName).build();

        //when
        ValidationException exception =
                assertThrows(ValidationException.class, () -> employeeService.updateEmployee(employee.getId(), updateEmployeeDTO));

        //then
        assertEquals("Name cannot contain illegal chars", exception.getMessage());
    }

    @Test
    void deleteEmployee_workingTest(){
        //given
        Store store = getDefaultSavedStore();
        Position position = getDefaultSavedPosition();

        String stay = "STAY";
        String delete = "DELETE";

        Employee employeeToStay = new TestEmployeeBuilder().withStore(store).withPosition(position).withFirstName(stay).build();
        employeeRepository.save(employeeToStay);

        Employee employeeToDelete = new TestEmployeeBuilder().withStore(store).withPosition(position).withFirstName(delete).build();
        employeeRepository.save(employeeToDelete);

        Long employeeToStayId = employeeToStay.getId();
        Long employeeToDeleteId = employeeToDelete.getId();

        //when
        employeeService.deleteEmployee(employeeToDeleteId);

        //then
        assertTrue(employeeRepository.existsById(employeeToStayId));

        assertFalse(employeeRepository.existsById(employeeToDeleteId));
    }

    @Test
    void deleteEmployee_employeeWithIdDoesNotExistThrowsException(){
        //given
        Long randomId = 1234L;

        //when
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> employeeService.deleteEmployee(randomId));

        //then
        assertEquals("Cannot find employee by id " + randomId, exception.getMessage());
    }

    @Test
    void findAll_workingTest(){
        //given
        Store store = getDefaultSavedStore();
        Position position = getDefaultSavedPosition();

        Employee first = new TestEmployeeBuilder().withStore(store).withPosition(position).withFirstName("FIRST").build();
        Employee second = new TestEmployeeBuilder().withStore(store).withPosition(position).withFirstName("SECOND").build();
        Employee third = new TestEmployeeBuilder().withStore(store).withPosition(position).withFirstName("THIRD").build();
        employeeRepository.saveAll(List.of(first,second,third));

        ResponseEmployeeDTO firstResponseEmployeeDTO = new TestResponseEmployeeDTO().fromEmployee(first).build();
        ResponseEmployeeDTO secondResponseEmployeeDTO = new TestResponseEmployeeDTO().fromEmployee(second).build();
        ResponseEmployeeDTO thirdResponseEmployeeDTO = new TestResponseEmployeeDTO().fromEmployee(third).build();
        List<ResponseEmployeeDTO> employeeDTOs = List.of(firstResponseEmployeeDTO,secondResponseEmployeeDTO,thirdResponseEmployeeDTO);

        //when
        List<ResponseEmployeeDTO> serviceResponse = employeeService.findAll();

        //then
        assertEquals(3,serviceResponse.size());
        assertTrue(serviceResponse.containsAll(employeeDTOs));
    }

    @Test
    void findAll_emptyListDoesNotThrowException(){
        //given

        //when
        List<ResponseEmployeeDTO> serviceResponse = employeeService.findAll();

        //then
        assertEquals(0, serviceResponse.size());
        assertDoesNotThrow(() -> employeeService.findAll());
    }

    private Store getDefaultSavedStore(){
        Region region = new TestRegionBuilder().build();
        regionRepository.save(region);

        Branch branch = new TestBranchBuilder().withRegion(region).build();
        branchRepository.save(branch);


        Store store = new TestStoreBuilder().withBranch(branch).build();
        return storeRepository.save(store);
    }

    private Position getDefaultSavedPosition(){
        Position position = new TestPositionBuilder().build();
        return positionRepository.save(position);
    }
}
