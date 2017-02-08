package pt.tecnico.mydrive.exception;

public class InvalidAppContentException extends MyDriveException {

    private static final long serialVersionUID = 1L;

    private String _content;

    public InvalidAppContentException(String content) {
        _content = content;
    }

    public String getInvalidcontent() {
        return _content;
    }

    @Override
    public String getMessage() {
        return "Invalid method name format: " + _content;
    }
}
