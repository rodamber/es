package pt.tecnico.mydrive.exception;

public class InvalidLinkContentException extends MyDriveException {

    private static final long serialVersionUID = 1L;

    private String _content;

    public InvalidLinkContentException(String content) {
        _content = content;
    }

    public String getInvalidcontent() {
        return _content;
    }

    @Override
    public String getMessage() {
        return "Invalid link: " + _content;
    }
}
