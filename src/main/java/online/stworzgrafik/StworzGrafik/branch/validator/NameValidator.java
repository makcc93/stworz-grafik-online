package online.stworzgrafik.StworzGrafik.branch.validator;

import jakarta.validation.ValidationException;
import online.stworzgrafik.StworzGrafik.exception.ArgumentNullChecker;

public final class NameValidator {

    public static String validate(String name){
        ArgumentNullChecker.check(name, "Name");

        if (!name.matches("^[a-zA-ZąćęłńóśźżĄĘĆŁŃÓŚŹŻ0-9\\t\\n ]+$")){
            throw new ValidationException("Branch name cannot contains illegal chars");
        }

        return name.strip().replaceAll("\\s+","").toUpperCase();
    }
}
