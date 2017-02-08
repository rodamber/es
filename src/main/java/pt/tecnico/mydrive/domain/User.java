package pt.tecnico.mydrive.domain;

import org.jdom2.Element;

import pt.tecnico.mydrive.exception.*;
import pt.tecnico.mydrive.utils.*;

public class User extends User_Base {
	private int passwordMinimumLength = 8;

    protected void init(final FileSystem fs, final String username,
                        final String name,   final String password,
                        String userHome,     final Permissions umask)
        throws UserInvalidUsernameException, IllegalArgumentException
    {
        if (fs == null)
            throw new IllegalArgumentException("FileSystem");
        if (username == null)
            throw new IllegalArgumentException("username");
        if (!validUsername(username))
            throw new UserInvalidUsernameException(username);
        final File rootDir  = fs.getRootDirectory();
        final RootUser root = fs.getRootUser();

        setFileSystem(fs);
        setUsername(username);
        setSessionDuration(2 * 60 * 60 * 1000, root); // 2 hours.

        setName(name != null ? name : username);
        setPassword(password != null ? password : username, this);

        setUmask(umask != null ? new Permissions(umask)
                               : new Permissions("rwxd----"));

        userHome = userHome != null ? userHome : username;

        final Directory homeDir = (Directory) rootDir.getFile("home", root);
        final Directory userHomeDir = new Directory(fs, homeDir, userHome, root);

        userHomeDir.setOwner(this);
        userHomeDir.setPermissions(new Permissions(getUmask()));

        setHomeDirectory(userHomeDir);
    }

    protected User() { super(); }

    public User(final FileSystem fs,   final String name,
                final String username, final String password,
                final String userHome, final Permissions umask)
        throws UserInvalidUsernameException, IllegalArgumentException
    {
        init(fs, name, username, password, userHome, umask);
    }

    public User(final FileSystem fs, final String username)
        throws UserInvalidUsernameException, IllegalArgumentException
    {
        this(fs, username, null, null, null, null);
    }

    public User(final FileSystem fs, final Element element) throws ImportDocumentException {
        final String username = element.getAttribute("username").getValue();

        final String name     = Utils.elementDefaultValue(element, "name", username);
        final String password = Utils.elementDefaultValue(element, "password", username);

        String home = Utils.elementDefaultValue(element, "home", "/home/" + username);
        home = home.substring(6, home.length());

        final String mask = Utils.elementDefaultValue(element, "mask", "rwxd----");

        init(fs, username, name, password, home, new Permissions(mask));
    }

    private boolean validUsername(String username){
        return username.matches("[A-Za-z0-9]+");
    }

    @Override
    public String getPassword() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPassword(String s) {
        throw new UnsupportedOperationException();
    }

    /* package */ boolean checkPassword(String password) 
            throws NoPasswordException {
        if (password == null){
            throw new NoPasswordException(getName());
        }
        return password.equals(super.getPassword());
    }

    public void setPassword(String newPassword, User user)
        throws AccessDeniedException, IllegalArgumentException, InvalidPasswordException
    {
        if (newPassword == null)
            throw new IllegalArgumentException("new password is null");
        if (user != this)
            throw new AccessDeniedException();
        if (newPassword.length() < passwordMinimumLength)
        	 throw new InvalidPasswordException("New password must be at least " + Integer.toString(passwordMinimumLength) + " characters long");
        super.setPassword(newPassword);
    }

    public void setPassword(String newPassword, RootUser ru)
        throws IllegalArgumentException
    {
        if (newPassword == null)
            throw new IllegalArgumentException("new password is null");
        super.setPassword(newPassword);
    }

    @Override
    public void setSessionDuration(long d) {
        throw new UnsupportedOperationException();
    }

    public void setSessionDuration(long d, RootUser ru) {
        super.setSessionDuration(d);
    }

    @Override
    public void setFileSystem(FileSystem fs) throws UserExistsException {
        if (fs == null)
            super.setFileSystem(fs);
        else
            fs.addUser(this);
    }

    public void remove() {
        setFileSystem(null);
        // RODRIGO:FIXME: setSession(null);
        setHomeDirectory(null);
        setUmask(null);
    }

    public Element xmlExport() {
        Element element = new Element("user");

        element.setAttribute("username", getUsername());

        element.addContent(new Element("name").addContent(getName()));
        element.addContent(new Element("password").addContent(super.getPassword()));
        element.addContent(new Element("mask").addContent(getUmask().toString()));
        element.addContent(new Element("home").addContent(getHomeDirectory().getAbsolutePath()));

        return element;
    }

    protected boolean hasDeletePermission(File file) {
        if (this == file.getOwner())
            return file.getPermissions().getOwnerDeletePermission() &&
                file.getParentDirectory().getPermissions().getOwnerExecutePermission();
        return file.getPermissions().getOthersDeletePermission() &&
            file.getParentDirectory().getPermissions().getOthersExecutePermission();
    }


    protected boolean hasReadPermission(File file) {
        if (this == file.getOwner())
           return file.getPermissions().getOwnerReadPermission() &&
                file.getParentDirectory().getPermissions().getOwnerExecutePermission();
        return file.getPermissions().getOthersReadPermission() &&
            file.getParentDirectory().getPermissions().getOthersExecutePermission();
    }

    protected boolean hasWritePermission(File file) {
        if (this == file.getOwner())
            return file.getPermissions().getOwnerWritePermission() &&
                file.getParentDirectory().getPermissions().getOwnerExecutePermission();
        return file.getPermissions().getOthersWritePermission() &&
            file.getParentDirectory().getPermissions().getOthersExecutePermission();
    }

    protected boolean hasExecutePermission(File file) {
        if (this == file.getOwner())
            return file.getPermissions().getOwnerExecutePermission() &&
                file.getParentDirectory().getPermissions().getOwnerExecutePermission();
        return file.getPermissions().getOthersExecutePermission() &&
            file.getParentDirectory().getPermissions().getOthersExecutePermission();
    }

    protected boolean hasPathExecutePermission(File file) {
        File dir = getFileSystem().getRootDirectory();
        String path = file.getAbsolutePath();

        String[] dirs = path.split("/");

        for(int i = 1; i < dirs.length - 1 ; i++) {
            // First directory is blank (because it's an absolute path), and the last is the filename.
            dir = dir.getFile(dirs[i], getFileSystem().getRootUser());
            if(!(hasExecutePermission(dir)))
                return false;
        }
        return true;
    }
   
    protected void verifySessionPassword() throws InvalidPasswordException {
    	if(super.getPassword().length() < passwordMinimumLength){
    		throw new InvalidPasswordException("Password must be at least " + Integer.toString(passwordMinimumLength) + " characters long in order to start a session. Current password is " + Integer.toString(super.getPassword().length()) + " characters long. ");
    	}
    }
    
        
    public FileExtension getFileExtension(String name) {
        for (FileExtension ext: getFileExtensionSet())
            if (ext.getName().equals(name))
                return ext;
        throw new FileExtensionUnknownException(name);
    }

    public boolean hasFileExtension(String name) {
        try {
            getFileExtension(name);
        } catch (FileExtensionUnknownException e) {
            return false;
        }
        return true;
    }

    @Override
    public void addFileExtension(FileExtension ext){
        try {
            FileExtension prevExt = getFileExtension(ext.getName());
            prevExt.setApp(ext.getApp()); // update app
        } catch (FileExtensionUnknownException e) {
            super.addFileExtension(ext);
        }
               
    }

}
