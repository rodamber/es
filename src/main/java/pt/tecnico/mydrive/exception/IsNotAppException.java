package pt.tecnico.mydrive.exception;

public class IsNotAppException extends MyDriveException {

    private static final long serialVersionUID = 1L;

    private String _fileName;

    public IsNotAppException(String fileName) {
        _fileName = fileName;
    }

    public String getAppName() {
        return _fileName;

    }

    @Override
    public String getMessage() {
        return "File " + _fileName + " is not an App";
    }
}