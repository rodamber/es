package pt.tecnico.mydrive.exception;

public class InvalidPasswordException extends MyDriveException {

    private static final long serialVersionUID = 1L;
    private final String message;

    public InvalidPasswordException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
