package online.stworzgrafik.StworzGrafik.employee;

import online.stworzgrafik.StworzGrafik.employee.position.Position;
import online.stworzgrafik.StworzGrafik.store.Store;
import org.springframework.stereotype.Component;

@Component
final class EmployeeBuilder {
    public Employee createEmployee(
            String firstName,
            String lastName,
            Long sap,
            Store store,
            Position position){

        return Employee.builder()
                .firstName(firstName)
                .lastName(lastName)
                .sap(sap)
                .store(store)
                .position(position)
                .build();
    }
}
