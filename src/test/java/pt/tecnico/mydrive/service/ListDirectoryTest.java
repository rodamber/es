package pt.tecnico.mydrive.service;

import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Random;
import java.util.Set;

import org.joda.time.DateTime;
import org.junit.Test;

import pt.tecnico.mydrive.domain.*;
import pt.tecnico.mydrive.exception.*;
import pt.tecnico.mydrive.service.ListDirectoryService;
import pt.tecnico.mydrive.service.dto.EntryDto;

public class ListDirectoryTest extends AbstractServiceTest{

    long rootTok, ownerTok, otherTok;
    Login login;
    User root, owner, other;
    Directory ofeliaHome, ofeliaDocuments;
    String ownerName = "Ofelia", otherName = "Jeremias";

    protected void populate(){
        FileSystem fs = FileSystem.getInstance();
        login = fs.getLogin();

        root = fs.getRootUser();
        owner = new User(fs, ownerName, ownerName, ownerName+"_password", null, null);
        other = new User(fs, otherName, otherName, otherName+"_password", null, null);

        rootTok = fs.login(root.getUsername(), "***");
        ownerTok = fs.login(owner.getUsername(), ownerName+"_password");
        otherTok = fs.login(other.getUsername(), otherName+"_password");

        ofeliaHome = (Directory) fs.getRootDirectory().getFile("/home/Ofelia", root);

        ofeliaDocuments = new Directory(fs, ofeliaHome, "Documents", owner);
        new PlainFile(fs, ofeliaDocuments, "File1", owner);
        new PlainFile(fs, ofeliaDocuments, "File2", owner);
        new PlainFile(fs, ofeliaDocuments, "File3", owner);

    }
    // 1. Root user lists a directory (succeeds)
    @Test
    public void rootListDirectory(){
        login.sessionChangeWorkingDirectory(rootTok, "/home/Ofelia/Documents");

        ListDirectoryService service = new ListDirectoryService(rootTok, ".");
        service.execute();

        final Directory cwd = login.sessionGetWorkingDirectory(rootTok);
        for (EntryDto dto : service.result()) {
            assertTrue("Wrong directory listing", cwd.hasFile(dto.fileName, root));
        }
    }

    // 2. Owner lists a directory (succeeds)
    @Test
    public void ownerListDirectory(){
        login.sessionChangeWorkingDirectory(ownerTok, "/home/Ofelia/Documents");

        ListDirectoryService service = new ListDirectoryService(ownerTok, ".");
        service.execute();

        final Directory cwd = login.sessionGetWorkingDirectory(ownerTok);
        for (EntryDto dto : service.result()) {
            assertTrue("Wrong directory listing", cwd.hasFile(dto.fileName, root));
        }
    }

    // 3. Other user lists a directory (fails)
    @Test(expected = AccessDeniedException.class)
    public void otherListDirectory(){
        login.sessionChangeWorkingDirectory(otherTok, "/home/Ofelia/Documents");

        ListDirectoryService service = new ListDirectoryService(otherTok, ".");
        service.execute();
    }

    // 4. Root user lists a public directory (succeeds)
    @Test
    public void rootListPublicDirectory(){
        ofeliaHome.getFile("Documents", owner).getPermissions().setPermissions(owner, "rwxdrwxd");
        login.sessionChangeWorkingDirectory(rootTok, "/home/Ofelia/Documents");

        ListDirectoryService service = new ListDirectoryService(rootTok, ".");
        service.execute();

        final Directory cwd = login.sessionGetWorkingDirectory(rootTok);
        for (EntryDto dto : service.result()) {
            assertTrue("Wrong directory listing", cwd.hasFile(dto.fileName, root));
        }
    }

    // 5. Owner lists a public directory (succeeds)
    @Test
    public void ownerListPublicDirectory(){
        ofeliaHome.getFile("Documents", owner).getPermissions().setPermissions(owner, "rwxdrwxd");
        login.sessionChangeWorkingDirectory(ownerTok, "/home/Ofelia/Documents");

        ListDirectoryService service = new ListDirectoryService(ownerTok, ".");
        service.execute();

        final Directory cwd = login.sessionGetWorkingDirectory(ownerTok);
        for (EntryDto dto : service.result()) {
            assertTrue("Wrong directory listing", cwd.hasFile(dto.fileName, root));
        }
    }

    // 6. Other user lists a public directory (succeeds)
    @Test
    public void otherListPublicDirectory(){
        ofeliaHome.getFile("Documents", owner).getPermissions().setPermissions(owner, "rwxdrwxd");
        ofeliaHome.getPermissions().setPermissions(owner, "rwxdrwxd");
        login.sessionChangeWorkingDirectory(otherTok, "/home/Ofelia/Documents");

        ListDirectoryService service = new ListDirectoryService(otherTok, ".");
        service.execute();

        final Directory cwd = login.sessionGetWorkingDirectory(otherTok);
        for (EntryDto dto : service.result()) {
            assertTrue("Wrong directory listing", cwd.hasFile(dto.fileName, root));
        }
    }

    // 7. Root user lists a private directory (succeeds)
    @Test
    public void rootListPrivateDirectory(){
        ofeliaHome.getFile("Documents", owner).getPermissions().setPermissions(owner, "--------");
        login.sessionChangeWorkingDirectory(rootTok, "/home/Ofelia/Documents");

        ListDirectoryService service = new ListDirectoryService(rootTok, ".");
        service.execute();

        final Directory cwd = login.sessionGetWorkingDirectory(rootTok);
        for (EntryDto dto : service.result()) {
            assertTrue("Wrong directory listing", cwd.hasFile(dto.fileName, root));
        }
    }

    // 8. Owner lists a private directory (fails)
    @Test(expected = AccessDeniedException.class)
    public void ownerListPrivateDirectory(){
        ofeliaHome.getFile("Documents", owner).getPermissions().setPermissions(owner, "--------");
        login.sessionChangeWorkingDirectory(ownerTok, "/home/Ofelia/Documents");

        ListDirectoryService service = new ListDirectoryService(ownerTok, ".");
        service.execute();
    }

    // 9. Other user lists a private directory (fails)
    @Test(expected = AccessDeniedException.class)
    public void otherListPrivateDirectory(){
        ofeliaHome.getFile("Documents", owner).getPermissions().setPermissions(owner, "--------");
        login.sessionChangeWorkingDirectory(otherTok, "/home/Ofelia/Documents");

        ListDirectoryService service = new ListDirectoryService(otherTok, ".");
        service.execute();
    }

    //10. owner with expired session tries to list a directory, exception thrown
    @Test (expected = SessionExpiredException.class)
    public void expiredSessionListDirectory() {
		login.sessionChangeWorkingDirectory(ownerTok, "/home/Ofelia/Documents");
        DateTime now = new DateTime();

        login.getSession(ownerTok).setExpiryDate(now.minusHours(3));

        ListDirectoryService service = new ListDirectoryService(ownerTok, ".");
        service.execute();
    }

    //11. owner without login tries to list a directory, exception thrown
    @Test (expected = SessionUnknownException.class)
    public void noSessionListDirectory() {
		login.sessionChangeWorkingDirectory(ownerTok, "/home/Ofelia/Documents");
        login.getSession(ownerTok).remove();

        ListDirectoryService service = new ListDirectoryService(ownerTok, ".");
        service.execute();
    }

}
