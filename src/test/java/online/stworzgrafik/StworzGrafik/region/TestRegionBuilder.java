package online.stworzgrafik.StworzGrafik.region;

public class TestRegionBuilder {
    private String name = "TESTREGION";

    public Region build(){
        return new RegionBuilder().createRegion(
                name
        );
    }
}
