package online.stworzgrafik.StworzGrafik.region;
//package online.stworzgrafik.StworzGrafik.employee.position.region;

public class TestRegionBuilder {
    private String name = "TESTREGION";

    public TestRegionBuilder withName (String name){
        this.name = name;
        return this;
    }

    public Region build(){
        return new RegionBuilder().createRegion(
                name
        );
    }
}
