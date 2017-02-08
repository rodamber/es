package pt.tecnico.mydrive.domain;

import pt.tecnico.mydrive.exception.AccessDeniedException;
import pt.tecnico.mydrive.exception.IllegalRemovalException;

public class RootUser extends RootUser_Base {

    public RootUser(FileSystem fs) {
        if (fs == null)
            throw new IllegalArgumentException("FileSystem");

        setFileSystem(fs);
        setRootUserFileSystem(fs);
        setName("Super User");
        setUsername("root");
        setPassword( "***", this);
        setUmask(new Permissions("rwxdr-x-"));
        setSessionDuration(10 * 60 * 1000, this); // 10 minutes.
    }

    @Override
    protected boolean hasWritePermission(File file) {
        return true;
    }

    @Override
    public void remove() {
        throw new IllegalRemovalException(getUsername());
    }

    @Override
    protected boolean hasReadPermission(File file) {
        return true;
    }


    @Override
    protected boolean hasDeletePermission(File file) {
        return true;
    }

    @Override
    protected boolean hasExecutePermission(File file) {
        return true;
    }

    @Override
    protected boolean hasPathExecutePermission(File file) {
        return true;
    }
    
    protected void verifySessionPassword() {
    	return;
    }
    
    public void setPassword(String newPassword, User user) {
    	super.setPassword(newPassword);
    }

}
