package online.stworzgrafik.StworzGrafik.validator;

import org.springframework.stereotype.Component;

@Component
public class PersonNameValidator implements NameValidatorStrategy {

    @Override
    public String validate(String name) {
        return name.toUpperCase();
    }

    @Override
    public ObjectType getSupportedType() {
        return ObjectType.PERSON;
    }
}
