package online.stworzgrafik.StworzGrafik.validator;

import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;

@Service
public class SapNameValidator implements NameValidatorStrategy{
    @Override
    public String validate(String name) {
        if (!name.matches("^[0-9\\t\\n\\s ]+$")){
            throw new ValidationException("Sap number cannot contains illegal chars");
        }

        return name.strip().replaceAll("\\s","");
    }

    @Override
    public ObjectType getSupportedType() {
        return ObjectType.SAP;
    }
}
