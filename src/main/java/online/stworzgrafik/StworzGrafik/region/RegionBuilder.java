package online.stworzgrafik.StworzGrafik.region;

import online.stworzgrafik.StworzGrafik.validator.NameValidator;
import online.stworzgrafik.StworzGrafik.exception.ArgumentNullChecker;
import org.springframework.stereotype.Component;

@Component
public final class RegionBuilder {
    public Region createRegion(String name){
        ArgumentNullChecker.check(name,"Name");

        NameValidator.validate(name);

        return Region.builder()
                .name(name)
                .build();
    }

}
