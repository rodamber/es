package pt.tecnico.mydrive.service;

import org.joda.time.DateTime;

import static org.junit.Assert.*;
import org.junit.Test;

import pt.tecnico.mydrive.domain.*;
import pt.tecnico.mydrive.exception.*;

public class WriteFileTest extends AbstractServiceTest {
    Login login;

    @Override
    protected void populate() {
        FileSystem fs = FileSystem.getInstance();
        login = fs.getLogin();
        
        populateUsers();
        populateFiles();
    }

    private void populateUsers() {
        FileSystem fs = FileSystem.getInstance();

        User owner    = new User(fs, "owner", "owner", "owner_password", null, null);
        User nonOwner = new User(fs, "nonOwner", "nonOwner", "nonOwner_password", null, null);
        RootUser root = fs.getRootUser();
    }

    private void populateFiles() {
        FileSystem fs = FileSystem.getInstance();
        Directory  rd = fs.getRootDirectory();

        rd.getPermissions().setPermissions(fs.getRootUser(), "rwxdrwx-");

        User owner = getUser("owner");
        User nonOwner = getUser("nonOwner");

        File unknownFile = new PlainFile(fs, rd, "unknownFile", owner, "file to be removed");

        File fileForOwnerOnly = new PlainFile(fs, rd, "fileForOwnerOnly", owner, "write permission for owner only");

        File fileForEveryone = new PlainFile(fs, rd, "fileForEveryone", owner, "write permission for everyone");
        fileForEveryone.getPermissions().setOthersWritePermission(owner, true);
        fileForEveryone.getPermissions().setOthersReadPermission(owner, true);

        File fileForEveryoneExceptOwner = new PlainFile(fs, rd, "fileForEveryoneExceptOwner", owner,
                                                        "write permission for everyone except the owner");
        fileForEveryoneExceptOwner.getPermissions().setOwnerWritePermission(owner, false);
        fileForEveryoneExceptOwner.getPermissions().setOthersWritePermission(owner, true);
        fileForEveryoneExceptOwner.getPermissions().setOthersReadPermission(owner, true);

        File fileForNoOne = new PlainFile(fs, rd, "fileForNoOne", owner, "no write permission");
        fileForNoOne.getPermissions().setOwnerWritePermission(owner, false);

        File directory = new Directory(fs, rd, "directory", owner);
        
        // Links
        Directory ownerHome = owner.getHomeDirectory();
        
        PlainFile ownerPF = new PlainFile(fs, ownerHome, "ownerPlainFile", owner, "plain file in user home directory");
        Link ownerL = new Link(fs, ownerHome, "ownerLinkToPlainFile", owner, ownerPF.getAbsolutePath());
        new Link(fs, ownerHome, "ownerLinkToLink", owner, ownerL.getAbsolutePath());
        
        Directory ownerSubDirectory = new Directory(fs, ownerHome, "subdirectory", owner);
        new PlainFile(fs, ownerSubDirectory, "ownerSubPlainFile", owner, "plain file in user sub directory");
        new Link(fs, ownerHome, "ownerLinkToSubPlainFile", owner, "subdirectory/ownerSubPlainFile");
        
        // App
        new App(fs, ownerHome, "ownerApp", owner, "package.class.method");
        
    }

    private User getUser(String username) throws UserUnknownException {
        return FileSystem.getInstance().getUser(username);
    }

    private long login(String username, String password) throws UserUnknownException, WrongPasswordException {
        FileSystem fs = FileSystem.getInstance();
        Login login = fs.getLogin();

        User user  = getUser(username);
        long token = login.login(fs, user, password);

        login.sessionChangeWorkingDirectory(token, fs.getRootDirectory().getName());
        return token;
    }

    private File getFile(long token, String filename) throws EntryUnknownException {
        Session session = FileSystem.getInstance().getLogin().getSession(token);
        
        Directory cwd = session.getCwd();
        Directory root_dir = FileSystem.getInstance().getRootDirectory();

        if(filename.charAt(0) == '/')
            return root_dir.getFile(filename, session.getUser());
        else
            return cwd.getFile(filename, session.getUser());
        
    }

    /* Tests */

    @Test(expected = EntryUnknownException.class)
    public void rootShouldFailToWriteToUnknownFile() {
        final long token      = login("root", "***");
        final String filename = getFile(token, "unknownFile").getName();
        final String content  = "This should not be written";

        new DeleteFileService(token, filename).execute();
        new WriteFileService(token, filename, content).execute();
    }

    @Test(expected = SessionUnknownException.class)
    public void rootShouldFailToWriteWithUnknownSession() {
        final long token      = login("root", "***");
        final Login login     = FileSystem.getInstance().getLogin();
        final Session session = login.getSession(token);

        session.remove(); // RODRIGO:FIXME:This method is unsafe... but we don't have a logout service.

        final String filename = getFile(token, "fileForEveryone").getName();
        final String content  = "This should not be written";

        new WriteFileService(token, filename, content).execute();
    }

    @Test(expected = SessionExpiredException.class)
    public void rootShouldFailToWriteWithExpiredSession() {
        final long token = login("root", "***");

        final Login login = FileSystem.getInstance().getLogin();
        final Session session = login.getSession(token);

        session.setExpiryDate(new DateTime().minusHours(3));

        final String filename = getFile(token, "fileForEveryone").getName();
        final String content  = "This should not be written";

        new WriteFileService(token, filename, content).execute();
    }

    @Test
    public void ownerShouldBeAbleToWriteToPlainFileWithPermissionForEveryone() {
        final long token = login("owner", "owner_password");
        final String content = "ownerShouldBeAbleToWriteToPlainFileWithPermissionForEveryone";

        new WriteFileService(token, "fileForEveryone", content).execute();

        User user = getUser("owner");
        String fileContent = getFile(token, "fileForEveryone").read(user);
        assertEquals("File not written or incorrectly written", content, fileContent);
    }

    @Test
    public void ownerShouldBeAbleToWriteToPlainFileWithPermissionForOwnerOnly() {
        final long token = login("owner", "owner_password");
        final String content = "ownerShouldBeAbleToWriteToPlainFileWithPermissionForOwnerOnly";

        new WriteFileService(token, "fileForOwnerOnly", content).execute();

        User user = getUser("owner");
        String fileContent = getFile(token, "fileForOwnerOnly").read(user);
        assertEquals("File not written or incorrectly written", content, fileContent);
    }

    @Test(expected = AccessDeniedException.class)
    public void ownerShouldFailToWriteToPlainFileWithPermissionForEveryoneExceptOwner() {
        final long token = login("owner", "owner_password");
        final String content = "ownerShouldFailToWriteToPlainFileWithPermissionForEveryoneExceptOwner";

        new WriteFileService(token, "fileForEveryoneExceptOwner", content).execute();
    }

    @Test(expected = AccessDeniedException.class)
    public void ownerShouldFailToWriteToPlainFileWithNoPermission() {
        final long token = login("owner", "owner_password");
        final String content = "ownerShouldFailToWriteToPlainFileWithNoPermission";

        new WriteFileService(token, "fileForNoOne", content).execute();
    }

    @Test
    public void nonOwnerShouldBeAbleToWriteToPlainFileWithPermissionForEveryone() {
        final long token = login("nonOwner", "nonOwner_password");
        final String content = "nonOwnerShouldBeAbleToWriteToPlainFileWithPermissionForEveryone";

        new WriteFileService(token, "fileForEveryone", content).execute();

        User user = getUser("nonOwner");
        String fileContent = getFile(token, "fileForEveryone").read(user);
        assertEquals("File not written or incorrectly written", content, fileContent);
    }

    @Test(expected = AccessDeniedException.class)
    public void nonOwnerShouldFailToWriteToPlainFileWithPermissionForOwnerOnly() {
        final long token = login("nonOwner", "nonOwner_password");
        final String content = "nonOwnerShouldFailToWriteToPlainFileWithPermissionForOwnerOnly";

        new WriteFileService(token, "fileForOwnerOnly", content).execute();
    }

    @Test
    public void nonOwnerShouldBeAbleToWriteToPlainFileWithPermissionForEveryoneExceptOwner() {
        final long token = login("nonOwner", "nonOwner_password");
        final String content = "nonOwnerShouldBeAbleToWriteToPlainFileWithPermissionForEveryoneExceptOwner";

        new WriteFileService(token, "fileForEveryoneExceptOwner", content).execute();

        User user = getUser("nonOwner");
        String fileContent = getFile(token, "fileForEveryoneExceptOwner").read(user);
        assertEquals("File not written or incorrectly written", content, fileContent);
    }

    @Test(expected = AccessDeniedException.class)
    public void nonOwnerShouldFailToWriteToPlainFileWithNoPermission() {
        final long token = login("nonOwner", "nonOwner_password");
        final String content = "nonOwnerShouldFailToWriteToPlainFileWithNoPermission";

        new WriteFileService(token, "fileForNoOne", content).execute();
    }

    @Test
    public void rootShouldBeAbleToWriteToPlainFileWithPermissionForEveryone() {
        final long token = login("root", "***");
        final String content = "rootShouldBeAbleToWriteToPlainFileWithPermissionForEveryone";

        new WriteFileService(token, "fileForEveryone", content).execute();

        User user = getUser("root");
        String fileContent = getFile(token, "fileForEveryone").read(user);
        assertEquals("File not written or incorrectly written", content, fileContent);
    }

    @Test
    public void rootShouldBeAbleToWriteToPlainFileWithPermissionForOwnerOnly() {
        final long token = login("root", "***");
        final String content = "rootShouldBeAbleToWriteToPlainFileWithPermissionForOwnerOnly";

        new WriteFileService(token, "fileForOwnerOnly", content).execute();

        User user = getUser("root");
        String fileContent = getFile(token, "fileForOwnerOnly").read(user);
        assertEquals("File not written or incorrectly written", content, fileContent);
    }

    @Test
    public void rootShouldBeAbleToWriteToPlainFileWithPermissionForEveryoneExceptOwner() {
        final long token = login("root", "***");
        final String content = "rootShouldBeAbleToWriteToPlainFileWithPermissionForEveryoneExceptOwner";

        new WriteFileService(token, "fileForEveryoneExceptOwner", content).execute();

        User user = getUser("root");
        String fileContent = getFile(token, "fileForEveryoneExceptOwner").read(user);
        assertEquals("File not written or incorrectly written", content, fileContent);
    }

    @Test
    public void rootShouldBeAbleToWriteToPlainFileWithNoPermission() {
        final long token = login("root", "***");
        final String content = "rootShouldBeAbleToWriteToPlainFileWithNoPermission";

        new WriteFileService(token, "fileForNoOne", content).execute();

        User user = getUser("root");
        String fileContent = getFile(token, "fileForNoOne").read(user);
        assertEquals("File not written or incorrectly written", content, fileContent);
    }

    @Test(expected = IsNotPlainFileException.class)
    public void rootShouldFailToWriteToDirectory() {
        final long token = login("root", "***");
        final String content = "rootShouldFailToWriteToDirectory";

        new WriteFileService(token, "directory", content).execute();
    }
    
    @Test
    public void ownerShouldBeAbleToWriteOnLinkWithAbsPath() {
        final long token = login("owner", "owner_password");
        login.sessionChangeWorkingDirectory(token, "/home/owner");
        
        final String content = "ownerShouldBeAbleToWriteOnLinkWithAbsPath";
        WriteFileService service = new WriteFileService(token, "ownerLinkToPlainFile", content);
        service.execute();
        
        User user = getUser("owner");
        String linkContent = getFile(token, "/home/owner/ownerPlainFile").read(user);
        assertEquals("File not written or incorrectly written", content, linkContent);
    }
    
    @Test
    public void ownerShouldBeAbleToWriteOnLinkWithRelPath() {
        final long token = login("owner", "owner_password");
        login.sessionChangeWorkingDirectory(token, "/home/owner");
        
        final String content = "ownerShouldBeAbleToWriteOnLinkWithRelPath";
        WriteFileService service = new WriteFileService(token, "ownerLinkToSubPlainFile", content);
        service.execute();
        
        User user = getUser("owner");
        String linkContent = getFile(token, "/home/owner/subdirectory/ownerSubPlainFile").read(user);
        assertEquals("File not written or incorrectly written", content, linkContent);
    }
    
    @Test
    public void ownerShouldBeAbleToWriteOnLinkToLink() {
        final long token = login("owner", "owner_password");
        login.sessionChangeWorkingDirectory(token, "/home/owner");
        
        final String content = "ownerShouldBeAbleToWriteOnLinkToLink";
        WriteFileService service = new WriteFileService(token, "ownerLinkToLink", content);
        service.execute();
        
        User user = getUser("owner");
        String linkContent = getFile(token, "/home/owner/ownerPlainFile").read(user);
        assertEquals("File not written or incorrectly written", content, linkContent);
    }
    
    @Test
    public void ownerShouldBeAbleToWriteOnApp() {
        final long token = login("owner", "owner_password");
        login.sessionChangeWorkingDirectory(token, "/home/owner");

        final String content = "ownerShouldBeAbleToWriteOnApp";
        
        WriteFileService service = new WriteFileService(token, "ownerApp", content);
        service.execute();
        
        User user = getUser("owner");
        String appContent = getFile(token, "/home/owner/ownerApp").read(user);
        assertEquals("File not written or incorrectly written", content, appContent);
    }

}
