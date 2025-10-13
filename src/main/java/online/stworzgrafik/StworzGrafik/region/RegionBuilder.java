package online.stworzgrafik.StworzGrafik.region;

import online.stworzgrafik.StworzGrafik.validator.NameValidatorOLD;
import online.stworzgrafik.StworzGrafik.exception.ArgumentNullChecker;
import org.springframework.stereotype.Component;

@Component
public final class RegionBuilder {
    public Region createRegion(String name){
        ArgumentNullChecker.check(name,"Name");

        String validated = NameValidatorOLD.validate(name);

        return Region.builder()
                .name(validated)
                .build();
    }

}
