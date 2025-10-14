package online.stworzgrafik.StworzGrafik.validator;

import jakarta.validation.ValidationException;

public class StoreNameValidator implements NameValidatorStrategy{
    @Override
    public String validate(String name) {
        if (!name.matches("^[a-zA-ZąćęłńóśźżĄĘĆŁŃÓŚŹŻ -]+$")){
            throw new ValidationException("Name cannot contains illegal chars");
        }

        return name.strip().toUpperCase();
    }

    @Override
    public ObjectType getSupportedType() {
        return ObjectType.STORE;
    }
}
