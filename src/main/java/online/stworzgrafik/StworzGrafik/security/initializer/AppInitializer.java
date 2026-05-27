package online.stworzgrafik.StworzGrafik.security.initializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.branch.BranchService;
import online.stworzgrafik.StworzGrafik.branch.DTO.CreateBranchDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.CreateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.Employee;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.employee.EmployeeService;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.CreatePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.PositionService;
import online.stworzgrafik.StworzGrafik.employee.vacation.DTO.CreateEmployeeVacationDTO;
import online.stworzgrafik.StworzGrafik.employee.vacation.EmployeeVacationService;
import online.stworzgrafik.StworzGrafik.region.DTO.CreateRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.ResponseRegionDTO;
import online.stworzgrafik.StworzGrafik.region.RegionService;
import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.StoreService;
import online.stworzgrafik.StworzGrafik.temporaryUser.AppUser;
import online.stworzgrafik.StworzGrafik.temporaryUser.AppUserService;
import online.stworzgrafik.StworzGrafik.temporaryUser.UserRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppInitializer implements CommandLineRunner{
    private final AppUserService appUserService;
    private final PasswordEncoder passwordEncoder;
    private final RegionService regionService;
    private final BranchService branchService;
    private final PositionService positionService;
    private final EmployeeEntityService employeeEntityService;
    private final EmployeeService employeeService;
    private final StoreService storeService;
    private final EmployeeVacationService employeeVacationService;

    @org.springframework.beans.factory.annotation.Value("${app.admin.login}")
    private String adminLogin;

    @org.springframework.beans.factory.annotation.Value("${app.admin.password}")
    private String adminPassword;

    @org.springframework.beans.factory.annotation.Value("${app.user.login}")
    private String userLogin;

    @org.springframework.beans.factory.annotation.Value("${app.user.password}")
    private String userPassword;

    private int[] twoWeeksvacation = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};

    @Override
    public void run(String...args){
        ResponseRegionDTO region = regionService.createRegion(new CreateRegionDTO("WSCHÓD"));
        branchService.createBranch(new CreateBranchDTO("WARSZAWA 3",region.id()));
        createAdmin();
        createUser();
        createPositions();
        ResponseStoreDTO store = storeService.createStore(new CreateStoreDTO("PUŁAWY", "F7", "Puławy", 1L));
    }

    private void createEmployees(ResponseStoreDTO store) {
        employeeService.createEmployee(store.id(),new CreateEmployeeDTO(
                "Damian",
                "Mrozicki",
                10000001L,
                1L
        ));

        employeeService.createEmployee(store.id(),new CreateEmployeeDTO(
                "Mateusz",
                "Kruk",
                10000002L,
                1L
        ));

        employeeService.createEmployee(store.id(),new CreateEmployeeDTO(
                "Monika",
                "Baran",
                10000003L,
                1L
        ));

        employeeService.createEmployee(store.id(),new CreateEmployeeDTO(
                "Filip",
                "Kamiński",
                10000004L,
                1L
        ));

        employeeService.createEmployee(store.id(),new CreateEmployeeDTO(
                "Martyna",
                "Nowicka",
                10000005L,
                1L
        ));

        employeeService.createEmployee(store.id(),new CreateEmployeeDTO(
                "Agata",
                "Warmińska",
                10000006L,
                1L
        ));

        employeeService.createEmployee(store.id(),new CreateEmployeeDTO(
                "Michał",
                "Woch",
                10000007L,
                1L
        ));

        employeeService.createEmployee(store.id(),new CreateEmployeeDTO(
                "Michał",
                "Kozik",
                10000008L,
                1L
        ));

        employeeService.createEmployee(store.id(),new CreateEmployeeDTO(
                "Marcin",
                "Wojas",
                10000009L,
                1L
        ));

        employeeService.createEmployee(store.id(),new CreateEmployeeDTO(
                "Marcin",
                "Przepiórka",
                10000010L,
                1L
        ));
        employeeService.createEmployee(store.id(),new CreateEmployeeDTO(
                "Wojciech",
                "Pietruszka",
                10000011L,
                1L
        ));

        employeeService.createEmployee(store.id(),new CreateEmployeeDTO(
                "Olga",
                "Darewicz",
                10000012L,
                1L
        ));

        employeeService.createEmployee(store.id(),new CreateEmployeeDTO(
                "Tomasz",
                "Zając",
                10000013L,
                1L
        ));

        employeeService.createEmployee(store.id(),new CreateEmployeeDTO(
                "Karolina",
                "Nakonieczna",
                10000014L,
                1L
        ));

        employeeService.createEmployee(store.id(),new CreateEmployeeDTO(
                "Emil",
                "Miazek",
                10000015L,
                1L
        ));
    }


    private void createPositions() {
        positionService.createPosition(new CreatePositionDTO("KIEROWNIK SKLEPU","Zarządza pracą danego sklepu oraz wszytskimi pracownikami jednostki"));
        positionService.createPosition(new CreatePositionDTO("KIEROWNIK SPRZEDAŻY","Zarządza pracą zespołu sprzedażowego w danej jednostce"));
        positionService.createPosition(new CreatePositionDTO("KIEROWNIK ZMIANY","Zarządza pracą zespołu sprzedażowego w danej jednostce oraz jednocześnie pełni rolę sprzedażową"));
        positionService.createPosition(new CreatePositionDTO("DORADCA KLIENTA","Pełni rolę sprzedawcy"));
        positionService.createPosition(new CreatePositionDTO("DORADCA KLIENTA MANAGER","Pełni rolę sprzedawcy, a także wspomaga pracę zespołu Kierowników w danym sklepie"));
        positionService.createPosition(new CreatePositionDTO("KASJER","Odpowiada za przyjmowanie płatności na kasie"));
        positionService.createPosition(new CreatePositionDTO("MAGAZYNIER","Odpowiada za przyjęcie i wysyłkę towaru na sklepie"));
    }

    private void createAdmin() {
        if (!appUserService.existsByLogin("admin")){
            AppUser admin = AppUser.builder()
                    .login(adminLogin)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(UserRole.ADMIN)
                    .build();
            appUserService.save(admin);
            log.info("Admin created");
            log.info("Admin login={}", admin.getLogin());
            log.info("Admin password={}", admin.getPassword());
        }
    }

    private void createUser() {
        if (!appUserService.existsByLogin(userLogin)){
            AppUser user = AppUser.builder()
                    .login(userLogin)
                    .password(passwordEncoder.encode(userPassword))
                    .role(UserRole.STORE_MANAGER)
                    .build();
            appUserService.save(user);
            log.info("User created");
            log.info("User login={}", user.getLogin());
            log.info("User password={}", user.getPassword());
        }
    }
}
