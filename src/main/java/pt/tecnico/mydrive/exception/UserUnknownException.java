package pt.tecnico.mydrive.exception;

public class UserUnknownException extends MyDriveException {

    private static final long serialVersionUID = 1L;

    private String _username;

    public UserUnknownException(String username) {
        _username = username;
    }

    public String getUsername() {
        return _username;
    }

    @Override
    public String getMessage() {
        return _username + " doesn't exist";
    }
}