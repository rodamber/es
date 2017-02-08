package pt.tecnico.mydrive.exception;

public class EnvVarUnknownException extends MyDriveException {

    private static final long serialVersionUID = 1L;

    private String _name;

    public EnvVarUnknownException(String name) {
        _name = name;
    }

    public String getEnvVarname() {
        return _name;
    }

    @Override
    public String getMessage() {
        return _name + " doesn't exist";
    }
}
