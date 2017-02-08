package pt.tecnico.mydrive.exception;

public class IllegalRemovalException extends MyDriveException {
    /*
      - Removal of . or ..
      - Removal of /
      - Removal of a non-empty directory
    */

    private static final long serialVersionUID = 1L;

    private String _directoryName;

    public IllegalRemovalException(String directoryName) {
        _directoryName = directoryName;
    }

    public String getDirectoryName() {
        return _directoryName;

    }

    @Override
    public String getMessage() {
        return "Directory " + _directoryName + " can't be removed";
    }
}
