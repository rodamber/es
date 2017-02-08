package pt.tecnico.mydrive.exception;

public class InvalidFileNameException extends MyDriveException {

    private static final long serialVersionUID = 1L;

    private String _fileName;

    public InvalidFileNameException(String fileName) {
        _fileName = fileName;
    }

    public String getInvalidFileName() {
        return _fileName;
    }

    @Override
    public String getMessage() {
        return "Invalid file name format: " + _fileName;
    }
}