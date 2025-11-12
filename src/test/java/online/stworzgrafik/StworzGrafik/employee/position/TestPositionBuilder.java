package online.stworzgrafik.StworzGrafik.employee.position;

public class TestPositionBuilder {
    private String name = "TESTPOSITION";
    private String description = "TESTDESCRIPTION";

    public TestPositionBuilder withName(String name){
        this.name = name;
        return this;
    }

    public TestPositionBuilder withDescription(String description){
        this.description = description;
        return this;
    }

    public Position build(){
        return new PositionBuilder().createPosition(
                name,
                description
        );
    }
}
