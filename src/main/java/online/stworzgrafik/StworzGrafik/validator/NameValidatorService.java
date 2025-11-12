package online.stworzgrafik.StworzGrafik.validator;

import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@Validated
public class NameValidatorService {
    private final Map<ObjectType, NameValidatorStrategy> validators = new EnumMap<>(ObjectType.class);

    public NameValidatorService(List<NameValidatorStrategy> strategies){
        for (NameValidatorStrategy strategy : strategies){
            validators.put(strategy.getSupportedType(),strategy);
        }
    }

    public String validate(@Valid String name, @Valid ObjectType objectType){
        NameValidatorStrategy validator = validators.get(objectType);

        if (validator == null) {
        throw new IllegalArgumentException("Cannot find type for " + objectType);
        }

        return validator.validate(name);
    }
}
