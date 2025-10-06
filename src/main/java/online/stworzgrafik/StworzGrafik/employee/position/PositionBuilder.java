package online.stworzgrafik.StworzGrafik.employee.position;

import online.stworzgrafik.StworzGrafik.exception.ArgumentNullChecker;
import online.stworzgrafik.StworzGrafik.validator.NameValidator;
import org.springframework.stereotype.Component;

@Component
public class PositionBuilder {
    public Position createPostion(String name){
        ArgumentNullChecker.check(name,"Name");

        String validated = NameValidator.validateForPosition(name);

        return Position.builder()
                .name(validated)
                .build();
    }
    //test it and then go for mapper and then for service(Impl)
}
