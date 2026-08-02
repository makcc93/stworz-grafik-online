package online.stworzgrafik.StworzGrafik.demo;

import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.branch.BranchService;
import online.stworzgrafik.StworzGrafik.employee.DTO.CreateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.EmployeeService;
import online.stworzgrafik.StworzGrafik.region.RegionService;
import online.stworzgrafik.StworzGrafik.security.AuthService;
import online.stworzgrafik.StworzGrafik.store.DTO.CreateStoreDTO;
import online.stworzgrafik.StworzGrafik.store.StoreService;
import online.stworzgrafik.StworzGrafik.user.AppUserService;
import online.stworzgrafik.StworzGrafik.user.DTO.AuthResponse;
import online.stworzgrafik.StworzGrafik.user.DTO.CreateUserRequest;
import online.stworzgrafik.StworzGrafik.user.DTO.LoginRequest;
import online.stworzgrafik.StworzGrafik.user.UserRole;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
class DemoServiceImpl implements DemoService {
    private final AppUserService appUserService;
    private final StoreService storeService;
    private final BranchService branchService;
    private final RegionService regionService;
    private final EmployeeService employeeService;
    private final AuthService authService;

    @Override
    public AuthResponse createAccount() {
        String demo = "DEMO";
        Long regionId = regionService.findByName(demo).id();
        Long branchId = branchService.findByName(demo).id();

        int randomNumber = ThreadLocalRandom.current().nextInt(20000000, 90000000);
        String storeCode = findFirstUnusedDemoStoreCode(storeService);

        Long demoStoreId = storeService.createStore(
                new CreateStoreDTO(
                        "SKLEP DEMO " + randomNumber,
                        storeCode,
                        "Demonstracyjna 1, 00-001 Warszawa",
                        branchId
                )
        ).id();

        String login = "demo-" + randomNumber;
        String rawPassword = login;

        Long userId = appUserService.createSystemUser(
                new CreateUserRequest(
                        login,
                        rawPassword,
                        UserRole.STORE_MANAGER,
                        demoStoreId,
                        branchId,
                        regionId,
                        null
                )
        ).id();

        createStoreEmployees(employeeService, demoStoreId, randomNumber);
        return authService.login(new LoginRequest(login, rawPassword));
    }

    private void createStoreEmployees(EmployeeService employeeService, Long storeId, int randomNumber) {
        List<String> firstName = new ArrayList<>(firstNames());
        List<String> lastName = new ArrayList<>(lastNames());

        int secondRandomDigit = (randomNumber / 1000000) % 10;
        int number = randomNumber;

        employeeService.createEmployeeSystem(
                storeId,
                new CreateEmployeeDTO(
                        firstName.get(secondRandomDigit),
                        lastName.get(secondRandomDigit),
                        (long) number,
                        1L
                )
        );

        secondRandomDigit++;
        number++;

        employeeService.createEmployeeSystem(
                storeId,
                new CreateEmployeeDTO(
                        firstName.get(secondRandomDigit),
                        lastName.get(secondRandomDigit),
                        (long) number,
                        2L
                )
        );

        secondRandomDigit++;
        number++;

        employeeService.createEmployeeSystem(
                storeId,
                new CreateEmployeeDTO(
                        firstName.get(secondRandomDigit),
                        lastName.get(secondRandomDigit),
                        (long) number,
                        2L
                )
        );

        secondRandomDigit++;
        number++;

        for (int i = 1; i <= 10; i++) {
            employeeService.createEmployeeSystem(
                    storeId,
                    new CreateEmployeeDTO(
                            firstName.get(secondRandomDigit),
                            lastName.get(secondRandomDigit),
                            (long) number,
                            4L
                    )
            );

            secondRandomDigit++;
            number++;
        }

        employeeService.createEmployeeSystem(
                storeId,
                new CreateEmployeeDTO(
                        firstName.get(secondRandomDigit),
                        lastName.get(secondRandomDigit),
                        (long) number,
                        6L
                )
        );

        secondRandomDigit++;
        number++;

        employeeService.createEmployeeSystem(
                storeId,
                new CreateEmployeeDTO(
                        firstName.get(secondRandomDigit),
                        lastName.get(secondRandomDigit),
                        (long) number,
                        7L
                )
        );
    }

    private String findFirstUnusedDemoStoreCode(StoreService storeService) {
        for (String code : getPolishLettersAndNumbers()) {
            String storeCode = "#" + code;

            if (!storeService.existsByStoreCode(storeCode)) {
                return storeCode;
            }
        }

        throw new IllegalStateException("Brak wolnych kodów sklepów demo - wszystkie zostały wykorzystane");
    }

    public static Set<String> getPolishLettersAndNumbers() {
        return Set.of(
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
                "M", "N", "O", "P", "R", "S", "T", "U", "W", "Y", "Z",
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"
        );
    }

    private Set<String> firstNames() {
        return Set.of(
                "Nikodem", "Zofia", "Antoni", "Hanna", "Jan", "Maria", "Aleksander", "Julia", "Franciszek", "Maja",
                "Szymon", "Emilia", "Leon", "Oliwia", "Jakub", "Mikołaj", "Alicja", "Lena", "Wojciech", "Laura",
                "Dawid", "Olga", "Mateusz", "Monika", "Damian", "Agnieszka", "Filip", "Martyna", "Emil", "Klara",
                "Piotr", "Dagmara", "Wojeciech", "Izabela"
        );
    }

    private Set<String> lastNames() {
        return Set.of(
                "Nowak", "Wójcik", "Kowalczyk", "Woźniak", "Mazur",
                "Krawczyk", "Pietrzak", "Król", "Cieślak", "Kaczmarek",
                "Zając", "Bąk", "Szymczak", "Marciniak", "Olejnik",
                "Kowal", "Lis", "Kozioł", "Stępień", "Włodarczyk",
                "Pirat", "Dąb", "Kruk", "Wrona", "Orzeł", "Niebo", "Koło",
                "Dzik", "Ząb", "Mak", "Rak", "Liść"
        );
    }
}