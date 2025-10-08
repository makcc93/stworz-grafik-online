package online.stworzgrafik.StworzGrafik.dataBuilderForTests.position;

import online.stworzgrafik.StworzGrafik.employee.position.Position;
import online.stworzgrafik.StworzGrafik.employee.position.PositionBuilder;

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
