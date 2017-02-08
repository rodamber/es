package pt.tecnico.mydrive.exception;

public class UserExistsException extends MyDriveException {

    private static final long serialVersionUID = 1L;

    private String _conflictingUsername;

    public UserExistsException(String conflictingUsername) {
        _conflictingUsername = conflictingUsername;
    }

    public String getConflictingUsername() {
        return _conflictingUsername;

    }

    @Override
    public String getMessage() {
        return "Username " + _conflictingUsername + " is already being used";
    }
}