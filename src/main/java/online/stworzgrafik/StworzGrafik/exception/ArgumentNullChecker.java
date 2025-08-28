package online.stworzgrafik.StworzGrafik.exception;

public final class ArgumentNullChecker {
    private ArgumentNullChecker(){}

    public static void check(Object argument){
        if (argument == null){
            throw new NullPointerException();
        }
    }

    public static void check(Object argument, String argumentName){
        if (argument == null){
            throw new NullPointerException(argumentName + " cannot be null");
        }
    }

    public static void checkAll(Object... arguments){
        for (Object argument : arguments) {
            check(argument);
        }
    }
}
