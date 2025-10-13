package online.stworzgrafik.StworzGrafik.validator;

import jakarta.validation.ValidationException;
import online.stworzgrafik.StworzGrafik.exception.ArgumentNullChecker;

public final class NameValidatorOLD {
    private NameValidatorOLD(){}

    public static String validate(String name){
        ArgumentNullChecker.check(name, "Name");

        if (!name.matches("^[a-zA-ZąćęłńóśźżĄĘĆŁŃÓŚŹŻ0-9\\t\\n ]+$")){
            throw new ValidationException("Name cannot contains illegal chars");
        }

        return name.strip().replaceAll("\\s+","").toUpperCase();
    }

    public static String validateForPosition(String name){
        ArgumentNullChecker.check(name, "Name");

        if (!name.matches("^[a-zA-ZąćęłńóśźżĄĘĆŁŃÓŚŹŻ\\t\\n ]+$")){
            throw new ValidationException("Name cannot contains illegal chars");
        }

        return name.trim().toUpperCase();
    }
}
