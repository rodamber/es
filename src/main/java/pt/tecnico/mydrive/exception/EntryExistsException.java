package pt.tecnico.mydrive.exception;

public class EntryExistsException extends MyDriveException {

    private static final long serialVersionUID = 1L;

    private String _entryName;

    public EntryExistsException(String entryName) {
        _entryName = entryName;
    }

    public String getEntryName() {
        return _entryName;
    }

    @Override
    public String getMessage() {
        return "Entry with name " + _entryName + " already exists in this directory";
    }
}