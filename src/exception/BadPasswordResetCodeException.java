package exception;

public class BadPasswordResetCodeException extends Exception {

    public BadPasswordResetCodeException (String message){
        super(message);
    }
}

