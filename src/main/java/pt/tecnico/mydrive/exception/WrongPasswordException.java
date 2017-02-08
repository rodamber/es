package pt.tecnico.mydrive.exception;

public class WrongPasswordException extends MyDriveException {

    private static final long serialVersionUID = 1L;

    private String _username;

    public WrongPasswordException(String username) {
        _username = username;
    }

    public String getUsername() {
        return _username;
    }

    @Override
    public String getMessage() {
        return "Wrong password for user " + _username + ". Try again!";
    }
}