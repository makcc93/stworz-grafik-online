package online.stworzgrafik.StworzGrafik.branch;

import online.stworzgrafik.StworzGrafik.exception.ArgumentNullChecker;
import org.springframework.stereotype.Component;

@Component
public final class BranchBuilder {

    public Branch createBranch(
            String name
    ){
        ArgumentNullChecker.check(name,"Name");
        String upperCaseName = upperCaseName(name);

        return Branch.builder()
                .name(upperCaseName)
                .build();
    }


    private String upperCaseName(String name) {
        return name.trim().toUpperCase();
    }

}
