package pt.tecnico.mydrive.domain;

import pt.tecnico.mydrive.exception.IllegalRemovalException;

public class GuestUser extends GuestUser_Base {

    public GuestUser(FileSystem fs) {
        if (fs == null)
            throw new IllegalArgumentException("FileSystem (fs) is null");

        final String username = "nobody";
        final String name = "Guest";

        setFileSystem(fs);
        setGuestUserFileSystem(fs);
        setUsername(username);
        setName(name);
        setUmask(new Permissions("rwxdr-x-"));

        // This user has no session time limit, but we don't want the property
        // to be undefined.
        final RootUser root = fs.getRootUser();
        setSessionDuration(0, root);

        // Password will be null, because the guest user has no password and
        // it cannot be changed.
        // setPassword("", this);

        // Create home directory
        final File rootDir = fs.getRootDirectory();
        final Directory homeDir = (Directory) rootDir.getFile("home", root);
        final Directory userHomeDir = new Directory(fs, homeDir, username, root);

        userHomeDir.setOwner(this);
        userHomeDir.setPermissions(new Permissions(getUmask()));

        setHomeDirectory(userHomeDir);
    }

    @Override
    public void setPassword(String newPassword, User user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPassword(String newPassword, RootUser user) {
        throw new UnsupportedOperationException();
    }

    @Override
    /* package */ boolean checkPassword(final String password) {
        return password == null;
    }


    @Override
    protected boolean hasWritePermission(File file) {
        if (file.getOwner() != this)
            return false;
        return super.hasWritePermission(file);
    }

    @Override
    protected boolean hasDeletePermission(File file) {
        if (file.getOwner() != this)
            return false;
        return super.hasDeletePermission(file);
    }

    @Override
    public void remove() {
        throw new IllegalRemovalException(getUsername());
    }
    
    @Override
    protected void verifySessionPassword() {
    	return;
    }
}
