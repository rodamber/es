package pt.tecnico.mydrive.exception;

public class AbsolutePathSizeLimitExceeded extends MyDriveException {

    private static final long serialVersionUID = 1L;
    private final int pathMaxLength;

    public AbsolutePathSizeLimitExceeded(int pathMaxLength) {
    	this.pathMaxLength = pathMaxLength;
    }

    @Override
    public String getMessage() {
        return "File's path exceeds maximum length of " + Integer.toString(pathMaxLength) + " characters";
    }
}