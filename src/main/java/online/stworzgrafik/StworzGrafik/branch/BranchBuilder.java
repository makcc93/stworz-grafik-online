package online.stworzgrafik.StworzGrafik.branch;

import online.stworzgrafik.StworzGrafik.validator.NameValidator;
import online.stworzgrafik.StworzGrafik.exception.ArgumentNullChecker;
import online.stworzgrafik.StworzGrafik.region.Region;
import org.springframework.stereotype.Component;

@Component
public final class BranchBuilder {

    public Branch createBranch(
            String name,
            Region region
    ){
        ArgumentNullChecker.check(name,"Name");
        String validatedName = NameValidator.validate(name);

        return Branch.builder()
                .name(validatedName)
                .region(region)
                .build();
    }
}
