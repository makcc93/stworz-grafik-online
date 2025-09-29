package online.stworzgrafik.StworzGrafik.dataBuilderForTests.region;

import online.stworzgrafik.StworzGrafik.region.Region;
import online.stworzgrafik.StworzGrafik.region.RegionBuilder;

public class TestRegionBuilder {
    private String name = "TESTREGION";

    public Region build(){
        return new RegionBuilder().createRegion(
                name
        );
    }
}
