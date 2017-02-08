package pt.tecnico.mydrive.exception;

public class IsNotPlainFileException extends MyDriveException {

    private static final long serialVersionUID = 1L;

    private String _fileName;

    public IsNotPlainFileException(String fileName) {
        _fileName = fileName;
    }

    public String getDirectoryName() {
        return _fileName;

    }

    @Override
    public String getMessage() {
        return "File " + _fileName + " is not a plain file";
    }
}