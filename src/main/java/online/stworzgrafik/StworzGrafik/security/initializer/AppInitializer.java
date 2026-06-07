package online.stworzgrafik.StworzGrafik.security.initializer;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.stworzgrafik.StworzGrafik.billing.BillingPeriodConfigService;
import online.stworzgrafik.StworzGrafik.billing.DTO.BillingPeriodConfigRequest;
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
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.DTO.ShiftTypeConfigRequest;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftCode;
import online.stworzgrafik.StworzGrafik.shift.shiftTypeConfig.ShiftTypeConfigService;
import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.DTO.ResponseStoreDTO;
import online.stworzgrafik.StworzGrafik.store.StoreService;
import online.stworzgrafik.StworzGrafik.temporaryUser.AppUser;
import online.stworzgrafik.StworzGrafik.temporaryUser.AppUserService;
import online.stworzgrafik.StworzGrafik.temporaryUser.UserRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
    private final ShiftTypeConfigService shiftTypeConfigService;
    private final BillingPeriodConfigService billingPeriodConfigService;

    @org.springframework.beans.factory.annotation.Value("${app.admin.login}")
    private String adminLogin;

    @org.springframework.beans.factory.annotation.Value("${app.admin.password}")
    private String adminPassword;

    @org.springframework.beans.factory.annotation.Value("${app.user.login}")
    private String userLogin;

    @org.springframework.beans.factory.annotation.Value("${app.user.password}")
    private String userPassword;

    @Override
    public void run(String...args){
        createAdmin();
        createStore();
        generateAllFifteenMinuteShifts();
    }

    private void createStore() {
        storeService.createStore(new CreateStoreDTO("PUŁAWY", "F7", "Puławy", 3L));
    }

    private void createStoreEmployees(Long storeId){
        employeeService.createEmployee(storeId,
                new CreateEmployeeDTO(
                        "Damian",
                        "Mrozicki",
                        10000001L,
                        1L
                ));

        employeeService.createEmployee(storeId,
                new CreateEmployeeDTO(
                        "Monika",
                        "Baran",
                        10000002L,
                        2L
                ));

        employeeService.createEmployee(storeId,
                new CreateEmployeeDTO(
                        "Mateusz",
                        "Kruk",
                        10000003L,
                        2L
                ));

        employeeService.createEmployee(storeId,
                new CreateEmployeeDTO(
                        "Filip",
                        "Kamiński",
                        10000004L,
                        5L
                ));

        employeeService.createEmployee(storeId,
                new CreateEmployeeDTO(
                        "Martyna",
                        "Nowicka",
                        10000005L,
                        5L
                ));

        employeeService.createEmployee(storeId,
                new CreateEmployeeDTO(
                        "Wojciech",
                        "Pietruszka",
                        10000006L,
                        4L
                ));

        employeeService.createEmployee(storeId,
                new CreateEmployeeDTO(
                        "Agata",
                        "Warmińska",
                        10000007L,
                        4L
                ));

        employeeService.createEmployee(storeId,
                new CreateEmployeeDTO(
                        "Michał",
                        "Woch",
                        10000008L,
                        4L
                ));

        employeeService.createEmployee(storeId,
                new CreateEmployeeDTO(
                        "Tomasz",
                        "Zając",
                        10000009L,
                        4L
                ));

        employeeService.createEmployee(storeId,
                new CreateEmployeeDTO(
                        "Michał",
                        "Kozik",
                        10000010L,
                        4L
                ));

        employeeService.createEmployee(storeId,
                new CreateEmployeeDTO(
                        "Marcin",
                        "Przepiórka",
                        10000011L,
                        4L
                ));

        employeeService.createEmployee(storeId,
                new CreateEmployeeDTO(
                        "Marcin",
                        "Wojas",
                        10000012L,
                        4L
                ));

        employeeService.createEmployee(storeId,
                new CreateEmployeeDTO(
                        "Olga",
                        "Darewicz",
                        10000013L,
                        4L
                ));

        employeeService.createEmployee(storeId,
                new CreateEmployeeDTO(
                        "Karolina",
                        "Nakonieczna",
                        10000014L,
                        6L
                ));

        employeeService.createEmployee(storeId,
                new CreateEmployeeDTO(
                        "Emil",
                        "Miazek",
                        10000015L,
                        7L
                ));
    }


    private void generateAllFifteenMinuteShifts() {
        List<Shift> shifts = new ArrayList<>();
        int[] minutes = {0, 15, 30, 45};

        for (int startHour = 0; startHour <= 23; startHour++) {
            for (int startMinute : minutes) {
                LocalTime start = LocalTime.of(startHour, startMinute);
                for (int endHour = 0; endHour <= 23; endHour++) {
                    for (int endMinute : minutes) {
                        LocalTime end = LocalTime.of(endHour, endMinute);
                        if (start.isAfter(end)) continue;

                        Shift shift = new ShiftBuilder().createShift(start, end);

                        shifts.add(shift);
                    }
                }
            }
        }
        shiftEntityService.saveAll(shifts);
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
