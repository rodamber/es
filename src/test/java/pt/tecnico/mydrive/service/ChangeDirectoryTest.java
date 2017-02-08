package pt.tecnico.mydrive.service;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.Random;

import org.joda.time.DateTime;
import org.junit.Test;

import pt.tecnico.mydrive.domain.*;
import pt.tecnico.mydrive.exception.*;
import pt.tecnico.mydrive.service.ChangeDirectoryService;

public class ChangeDirectoryTest extends AbstractServiceTest{

    long rootTok;
    long ownerTok;
    long otherTok;

    Login login;

    protected void populate(){
        FileSystem fs = FileSystem.getInstance();
        login = fs.getLogin();

        User root = fs.getRootUser();
        User owner = new User(fs, "John", "John", "complicatedpassword", null, null);
        User other = new User(fs, "Peter", "Peter", "complicatedpassword2", null, null);

        rootTok = fs.login(root.getUsername(), "***");
        ownerTok = fs.login(owner.getUsername(), "complicatedpassword");
        otherTok = fs.login(other.getUsername(), "complicatedpassword2");

        Directory johnHome = (Directory) fs.getRootDirectory().getFile("/home/John", root);
        Directory music = new Directory(fs, johnHome, "music", owner);

        new PlainFile(fs, music, "PlainFile", owner, "Plain file content");
        new Link(fs, music, "Link", owner, "/home/John/music");
        new App(fs, music, "App", owner, "package.class.method");

    }

    // ABSOLUTE PATH TESTS

    // 1. Root changes to user directory using absolute path (succeeds)
    @Test
    public void rootChangeToUserDirWithAbsPath(){
        final String path = "/home/John/music";

        ChangeDirectoryService service = new ChangeDirectoryService(rootTok, path);
        service.execute();
        String resultCwd = service.result();

        assertEquals("Wrong current working directory", path, resultCwd);
    }

    // 2. Owner changes to user directory using absolute path (succeeds)
    @Test
    public void ownerChangeToUserDirWithAbsPath(){
        final String path = "/home/John/music";

        ChangeDirectoryService service = new ChangeDirectoryService(ownerTok, path);
        service.execute();
        String resultCwd = service.result();

        assertEquals("Wrong current working directory", path, resultCwd);
    }

    // 3. Other user changes to user home directory using absolute path (succeeds)
    @Test
    public void otherChangeToUserHomeWithAbsPath(){
        String path = "/home/John";

        ChangeDirectoryService service = new ChangeDirectoryService(otherTok, "/home/John");
        service.execute();
        String resultCwd = service.result();

        assertEquals("Wrong current working directory", path, resultCwd);
    }

    // 4. Other user changes to user directory using absolute path (fails)
    @Test(expected = AccessDeniedException.class)
    public void otherChangeToUserDirWithAbsPath(){
        ChangeDirectoryService service = new ChangeDirectoryService(otherTok, "/home/John/music");
        service.execute();
    }

    // RELATIVE PATH TESTS (Initial directory: /home)

    // 5. Root changes to user directory using relative path (succeeds)
    @Test
    public void rootChangeToUserDirWithRelPath(){
        login.sessionChangeWorkingDirectory(rootTok, "/home");

        final String path = "John/music";

        ChangeDirectoryService service = new ChangeDirectoryService(rootTok, path);
        service.execute();

        String resultCwd = service.result();
        assertEquals("Wrong current working directory", "/home/John/music", resultCwd);
    }

    // 6. Owner changes to user directory using relative path (succeeds)
    @Test
    public void ownerChangeToUserDirWithRelPath(){
        login.sessionChangeWorkingDirectory(ownerTok, "/home");

        final String path = "John/music";

        ChangeDirectoryService service = new ChangeDirectoryService(ownerTok, path);
        service.execute();

        String resultCwd = service.result();
        assertEquals("Wrong current working directory", "/home/John/music", resultCwd);
    }

    // 7. Other user changes to user directory using relative path (fails)
    @Test(expected = AccessDeniedException.class)
    public void otherChangeToUserDirWithRelPath(){
        login.sessionChangeWorkingDirectory(otherTok, "/home");

        ChangeDirectoryService service = new ChangeDirectoryService(otherTok, "John/music");
        service.execute();
    }

    // "." AND ".." TESTS

    // 8. User changes to his own current working directory (succeeds)
    @Test
    public void userChangeToCWD(){
        String cwd = login.sessionChangeWorkingDirectory(ownerTok, "/home/John").getAbsolutePath();

        ChangeDirectoryService service = new ChangeDirectoryService(ownerTok, ".");
        service.execute();
        String resultCwd = service.result();

        assertEquals("Wrong current working directory", cwd, resultCwd);
    }

    // 9. User changes to the parent of his own current working directory (succeeds)
    @Test
    public void userChangeToCWDParent(){
        String cwd = login.sessionChangeWorkingDirectory(ownerTok, "/home/John").getParentDirectory().getAbsolutePath();

        ChangeDirectoryService service = new ChangeDirectoryService(ownerTok, "..");
        service.execute();
        String resultCwd = service.result();

        assertEquals("Wrong current working directory", cwd, resultCwd);
    }

    // OTHER TESTS

    // 10. User changes to a unknown directory (fails)
    @Test(expected = EntryUnknownException.class)
    public void userChangeToUnknownEntry() {
        ChangeDirectoryService service = new ChangeDirectoryService(ownerTok, "/home/John/photos");
        service.execute();
    }

    // 11. User changes to a plainfile (fails)
    @Test(expected = IsNotDirectoryException.class)
    public void userChangeToPlainFile() {
        ChangeDirectoryService service = new ChangeDirectoryService(ownerTok, "/home/John/music/PlainFile");
        service.execute();
    }

    // 12. User changes to a link (fails)
    @Test(expected = IsNotDirectoryException.class)
    public void userChangeToLink() {
        ChangeDirectoryService service = new ChangeDirectoryService(ownerTok, "/home/John/music/Link");
        service.execute();
    }

    // 13. User changes to an app (fails)
    @Test(expected = IsNotDirectoryException.class)
    public void userChangeToApp() {
        ChangeDirectoryService service = new ChangeDirectoryService(ownerTok, "/home/John/music/App");
        service.execute();
    }

    // 14. User with a expired session tries to change directory (fails)
    @Test(expected = SessionExpiredException.class)
    public void userWithExpiredSession(){
        DateTime now = new DateTime();
        Session s = login.getSession(ownerTok);
        s.setExpiryDate(now.minusHours(3));

        ChangeDirectoryService service = new ChangeDirectoryService(ownerTok, "/home/John/music");
        service.execute();
    }

    // 15. User with a unknown session tries to change directory (fails)
    @Test(expected = SessionUnknownException.class)
    public void userWithUnknownSession(){
        login.getSession(ownerTok).remove();

        ChangeDirectoryService service = new ChangeDirectoryService(ownerTok, "/home/John/music");
        service.execute();
    }

}
