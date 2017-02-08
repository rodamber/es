package pt.tecnico.mydrive.exception;

public class ExtensionUnknownException extends MyDriveException {

    private static final long serialVersionUID = 1L;

    private String _extensionName;

    public ExtensionUnknownException(String extensionName) {
        _extensionName = extensionName;
    }

    public String getExtensionName() {
        return _extensionName;
    }

    @Override
    public String getMessage() {
        return "Extension " + _extensionName + " doesn't exist";
    }
}