package online.stworzgrafik.StworzGrafik.region;

import org.springframework.stereotype.Component;

@Component
public final class RegionBuilder {
    public Region createRegion(String name){
        return Region.builder()
                .name(name)
                .build();
    }

}
