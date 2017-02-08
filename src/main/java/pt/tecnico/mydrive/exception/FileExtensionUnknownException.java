package pt.tecnico.mydrive.exception;

public class FileExtensionUnknownException extends MyDriveException {

    private static final long serialVersionUID = 1L;

    private String _name;

    public FileExtensionUnknownException(String name) {
        _name = name;
    }

    public String getFileExtensionName() {
        return _name;
    }

    @Override
    public String getMessage() {
        return _name + " doesn't exist";
    }
}
