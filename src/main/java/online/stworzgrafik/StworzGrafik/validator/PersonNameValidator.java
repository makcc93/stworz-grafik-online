package online.stworzgrafik.StworzGrafik.validator;

import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;

@Service
public class PersonNameValidator implements NameValidatorStrategy {
    @Override
    public String validate(String name) {
        if (!name.matches("^[a-zA-ZąćęłńóśźżĄĘĆŁŃÓŚŹŻ -]+$")){
            throw new ValidationException("Name cannot contains illegal chars");
        }

        return name.strip().toUpperCase();
    }

    @Override
    public ObjectType getSupportedType() {
        return ObjectType.PERSON;
    }
}
