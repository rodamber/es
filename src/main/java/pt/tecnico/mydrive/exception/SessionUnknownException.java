package pt.tecnico.mydrive.exception;

public class SessionUnknownException extends MyDriveException {

    private static final long serialVersionUID = 1L;

    public SessionUnknownException() {
    }

    @Override
    public String getMessage() {
        return "You have to login first";
    }
}
