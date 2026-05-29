package online.stworzgrafik.StworzGrafik.security.initializer;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.branch.BranchService;
import online.stworzgrafik.StworzGrafik.branch.DTO.CreateBranchDTO;
import online.stworzgrafik.StworzGrafik.employee.DTO.CreateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.EmployeeEntityService;
import online.stworzgrafik.StworzGrafik.employee.EmployeeService;
import online.stworzgrafik.StworzGrafik.employee.position.DTO.CreatePositionDTO;
import online.stworzgrafik.StworzGrafik.employee.position.PositionService;
import online.stworzgrafik.StworzGrafik.employee.vacation.EmployeeVacationService;
import online.stworzgrafik.StworzGrafik.region.DTO.CreateRegionDTO;
import online.stworzgrafik.StworzGrafik.region.DTO.ResponseRegionDTO;
import online.stworzgrafik.StworzGrafik.region.RegionService;
import online.stworzgrafik.StworzGrafik.shift.Shift;
import online.stworzgrafik.StworzGrafik.shift.ShiftBuilder;
import online.stworzgrafik.StworzGrafik.shift.ShiftEntityService;
import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.StoreService;
import online.stworzgrafik.StworzGrafik.temporaryUser.AppUser;
import online.stworzgrafik.StworzGrafik.temporaryUser.AppUserService;
import online.stworzgrafik.StworzGrafik.temporaryUser.UserRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.ArrayList;
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
    private final ShiftEntityService shiftEntityService;

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
        ResponseRegionDTO region = createRegion();
        branchService.createBranch(new CreateBranchDTO("WARSZAWA 3",region.id()));
        createAdmin();
        createUser();
        createPositions();
        storeService.createStore(new CreateStoreDTO("PUŁAWY", "F7", "Puławy", 1L));
        generateAllFifteenMinuteShifts();
    }

    private ResponseRegionDTO createRegion() {
        String regionName = "WSCHÓD";

        if (!regionService.exists(regionName)) {
            return regionService.createRegion(new CreateRegionDTO(regionName));
        }

        return regionService.findAll().stream().filter(region -> region.name().equals(regionName)).findFirst().orElseThrow(EntityNotFoundException::new);
    }

    public void generateAllFifteenMinuteShifts() {
        List<Shift> shifts = new ArrayList<>();
        int[] minutes = {0, 15, 30, 45};

        for (int startHour = 0; startHour <= 23; startHour++) {
            for (int startMinute : minutes) {
                LocalTime start = LocalTime.of(startHour, startMinute);
                for (int endHour = 0; endHour <= 23; endHour++) {
                    for (int endMinute : minutes) {
                        LocalTime end = LocalTime.of(endHour, endMinute);
                        Shift shift = new ShiftBuilder().createShift(start, end);

                        shifts.add(shift);
                    }
                }
            }
        }
        shiftEntityService.saveAll(shifts);
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
