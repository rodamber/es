package pt.tecnico.mydrive.exception;

public class SessionExpiredException extends MyDriveException {

    private static final long serialVersionUID = 1L;

    public SessionExpiredException() {
    }

    @Override
    public String getMessage() {
        return "Your session has expired, please login again";
    }
}
