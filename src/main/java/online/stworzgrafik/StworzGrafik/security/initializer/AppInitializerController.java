package online.stworzgrafik.StworzGrafik.security.initializer;

import lombok.RequiredArgsConstructor;
import online.stworzgrafik.StworzGrafik.employee.DTO.CreateEmployeeDTO;
import online.stworzgrafik.StworzGrafik.employee.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/initializer")
@RequiredArgsConstructor
public class AppInitializerController {
    private final EmployeeService employeeService;

    @GetMapping("/createFirstStoreEmployees")
    ResponseEntity<HttpStatus> createFirstStoreEmployees(){
        createStoreEmployees(1L);

        return ResponseEntity.status(HttpStatus.CREATED).build();
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
}
