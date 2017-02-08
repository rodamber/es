package pt.tecnico.mydrive.exception;

public class NoPasswordException extends MyDriveException {

    private static final long serialVersionUID = 1L;

    private String _username;

    public NoPasswordException(String username) {
        _username = username;
    }

    public String getUsername() {
        return _username;
    }

    @Override
    public String getMessage() {
        return "Can't login user " + getUsername() + " without a password. Try again!";
    }
}