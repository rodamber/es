package pt.tecnico.mydrive.exception;

public class UserInvalidUsernameException extends MyDriveException {

    private static final long serialVersionUID = 1L;

    private String _invalidUsername;

    public UserInvalidUsernameException(String invalidUsername) {
        _invalidUsername = invalidUsername;
    }

    public String getInvalidUsername() {
        return _invalidUsername;

    }

    @Override
    public String getMessage() {
        return "Username " + _invalidUsername + " is invalid. It may only contain letters and digits.";
    }
}