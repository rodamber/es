package pt.tecnico.mydrive.exception;

public class EntryUnknownException extends MyDriveException {

    private static final long serialVersionUID = 1L;

    private String _entryName;

    public EntryUnknownException(String entryName) {
        _entryName = entryName;
    }

    public String getEntryName() {
        return _entryName;

    }

    @Override
    public String getMessage() {
        return "Entry with name " + _entryName + " doesn't exist in this directory";
    }
}