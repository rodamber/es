package pt.tecnico.mydrive.exception;

public class AccessDeniedException extends MyDriveException {

    private static final long serialVersionUID = 1L;

    public AccessDeniedException() {
    }

    @Override
    public String getMessage() {
        return "You don't have permission to do that";
    }
}