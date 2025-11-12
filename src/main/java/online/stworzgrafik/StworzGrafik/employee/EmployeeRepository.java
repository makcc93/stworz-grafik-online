package online.stworzgrafik.StworzGrafik.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface EmployeeRepository extends JpaRepository<Employee,Long>, JpaSpecificationExecutor<Employee> {
    boolean existsBySap(Long sap);
    boolean existsByLastName(String lastName);
}
