package online.stworzgrafik.StworzGrafik.employee.position;

import jakarta.annotation.Nullable;
import org.springframework.stereotype.Component;

@Component
public class PositionBuilder {
    public Position createPosition(
            String name,
            @Nullable String description){
        return Position.builder()
                .name(name)
                .description(description)
                .build();
    }
}
