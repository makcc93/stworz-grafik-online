package online.stworzgrafik.StworzGrafik.branch;

import online.stworzgrafik.StworzGrafik.region.Region;
import org.springframework.stereotype.Component;

@Component
public final class BranchBuilder {

    public Branch createBranch(
            String name,
            Region region
    ){
        return Branch.builder()
                .name(name)
                .region(region)
                .build();
    }
}
