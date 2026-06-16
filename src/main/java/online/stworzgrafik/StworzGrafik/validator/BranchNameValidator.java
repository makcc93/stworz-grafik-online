package online.stworzgrafik.StworzGrafik.validator;

import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;

@Service
public class BranchNameValidator implements NameValidatorStrategy {
    @Override
    public String validate(String name) {
        if (name == null) {
            throw new ValidationException("Name cannot be null");
        }

        String normalized = name.strip().replaceAll("\\s+", " ");

        if (normalized.isEmpty()) {
            throw new ValidationException("Name cannot be empty or contain only spaces");
        }

        if (!normalized.matches("^[a-zA-ZąćęłńóśźżĄĘĆŁŃÓŚŹŻ0-9 ]+$")) {
            throw new ValidationException("Name contains illegal characters");
        }

        return normalized.toUpperCase();
    }

    @Override
    public ObjectType getSupportedType() {
        return ObjectType.BRANCH;
    }
}
