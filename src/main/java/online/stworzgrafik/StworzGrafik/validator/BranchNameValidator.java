package online.stworzgrafik.StworzGrafik.validator;

import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;

@Service
public class BranchNameValidator implements NameValidatorStrategy {
    @Override
    public String validate(String name) {
        if (!name.matches("^[a-zA-ZąćęłńóśźżĄĘĆŁŃÓŚŹŻ0-9\\t\\n ]+$")){
            throw new ValidationException("Name cannot contain illegal chars");
        }

        return name.strip().replaceAll("\\s+","").toUpperCase();
    }

    @Override
    public ObjectType getSupportedType() {
        return ObjectType.BRANCH;
    }
}
