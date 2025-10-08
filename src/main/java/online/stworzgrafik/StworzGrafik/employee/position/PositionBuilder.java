package online.stworzgrafik.StworzGrafik.employee.position;

import jakarta.annotation.Nullable;
import online.stworzgrafik.StworzGrafik.exception.ArgumentNullChecker;
import online.stworzgrafik.StworzGrafik.validator.NameValidator;
import org.springframework.stereotype.Component;

@Component
public class PositionBuilder {
    public Position createPosition(String name, @Nullable String description){
        ArgumentNullChecker.check(name,"Name");

        String validated = NameValidator.validateForPosition(name);

        return Position.builder()
                .name(validated)
                .description(description)
                .build();
    }
}
