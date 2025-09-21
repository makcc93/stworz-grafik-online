package online.stworzgrafik.StworzGrafik.branch;

import online.stworzgrafik.StworzGrafik.branch.validator.NameValidator;
import online.stworzgrafik.StworzGrafik.exception.ArgumentNullChecker;
import org.springframework.stereotype.Component;

@Component
public final class BranchBuilder {

    public Branch createBranch(
            String name
    ){
        ArgumentNullChecker.check(name,"Name");
        String validatedName = NameValidator.validate(name);

        return Branch.builder()
                .name(validatedName)
                .build();
    }
}
