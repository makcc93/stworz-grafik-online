package online.stworzgrafik.StworzGrafik.branch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BranchRepository extends JpaRepository<Branch,Long>, JpaSpecificationExecutor<Branch> {
}
