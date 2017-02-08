package pt.tecnico.mydrive.exception;

public class IsNotDirectoryException extends MyDriveException {

    private static final long serialVersionUID = 1L;

    private String _fileName;

    public IsNotDirectoryException(String fileName) {
        _fileName = fileName;
    }

    public String getDirectoryName() {
        return _fileName;

    }

    @Override
    public String getMessage() {
        return "File " + _fileName + " is not a directory";
    }
}