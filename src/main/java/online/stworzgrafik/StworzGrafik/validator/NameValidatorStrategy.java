package online.stworzgrafik.StworzGrafik.validator;

public interface NameValidatorStrategy {
    String validate(String name);
    ObjectType getSupportedType();
}
