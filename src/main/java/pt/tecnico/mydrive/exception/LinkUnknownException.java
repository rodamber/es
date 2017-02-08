package pt.tecnico.mydrive.exception;

public class LinkUnknownException extends MyDriveException {

    private static final long serialVersionUID = 1L;

    private String _linkName;

    public LinkUnknownException(String linkName) {
        _linkName = linkName;
    }

    public String getLinkName() {
        return _linkName;
    }

    @Override
    public String getMessage() {
        return "Link " + _linkName + " doesn't exist";
    }
}
