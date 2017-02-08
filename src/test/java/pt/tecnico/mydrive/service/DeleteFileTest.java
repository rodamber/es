package pt.tecnico.mydrive.service;

import static org.junit.Assert.*;

import org.joda.time.DateTime;
import org.junit.Test;

import pt.tecnico.mydrive.domain.User;

import pt.tecnico.mydrive.domain.File;
import pt.tecnico.mydrive.domain.PlainFile;
import pt.tecnico.mydrive.domain.RootUser;
import pt.tecnico.mydrive.domain.App;
import pt.tecnico.mydrive.domain.Link;
import pt.tecnico.mydrive.domain.Directory;

import pt.tecnico.mydrive.domain.FileSystem;
import pt.tecnico.mydrive.domain.Login;
import pt.tecnico.mydrive.domain.Permissions;
import pt.tecnico.mydrive.service.DeleteFileService;

import pt.tecnico.mydrive.exception.AccessDeniedException;
import pt.tecnico.mydrive.exception.EntryUnknownException;
import pt.tecnico.mydrive.exception.IllegalRemovalException;
import pt.tecnico.mydrive.exception.SessionExpiredException;
import pt.tecnico.mydrive.exception.SessionUnknownException;

public class DeleteFileTest extends AbstractServiceTest {

    long token;
    long tokenRu;
    long tokenTony;
    RootUser ru;

    protected void populate() {
        FileSystem fs = FileSystem.getInstance();
        Login l = fs.getLogin();

        ru = fs.getRootUser();
        User e = new User(fs, "evaristo", "evaristo", "evaristo_password", null, null);
        User tony = new User(fs, "tony", "tony", "tony_password", null, null);

        token = fs.login(e.getUsername(), "evaristo_password");
        tokenRu = fs.login(ru.getUsername(), "***");
        tokenTony = fs.login(tony.getUsername(), "tony_password");

        // ======== evaristo files ========
        new PlainFile(fs, e.getHomeDirectory(), "plain", e, "nada");
        new Directory(fs, e.getHomeDirectory(), "empty", e);
        new App(fs, e.getHomeDirectory(), "app", e, "nada");
        new Link(fs, e.getHomeDirectory(), "link", e, "/home");

        Directory one_e = new Directory(fs, e.getHomeDirectory(), "one", e);
        new PlainFile(fs, one_e, "oneFile", e, "nada1");
        Directory two_e = new Directory(fs, one_e, "two", e);
        new PlainFile(fs, two_e, "twoFile", e, "nada2");

        // ======== tony files ========
        new PlainFile(fs, tony.getHomeDirectory(), "disto", tony, "nada");
        Directory carreira = new Directory(fs, tony.getHomeDirectory(), "carreira", tony);
        Directory c1 = new Directory(fs,carreira,"c1",tony); //"/home/tony/carreira/c1"

        new PlainFile(fs, c1, "tonyFile", tony, "nada");//"/home/tony/carreira/c1/tonyFile"


    }

    private Directory getWorkingDirectory(long token) {
        FileSystem fs = MyDriveService.getFileSystem();
        Login l = fs.getLogin();

        return l.sessionGetWorkingDirectory(token);
    }

    private Directory changeWorkingDirectory(long token, String path) {
        FileSystem fs = MyDriveService.getFileSystem();
        Login l = fs.getLogin();

        // sessionChangeWorkingDirectory must return Directory
        return l.sessionChangeWorkingDirectory(token, path);
    }

    private File getFile(long token, String fileName) {
        Directory workingDirectory = getWorkingDirectory(token);
        return workingDirectory.getFile(fileName,ru);
    }

    // 1. rootUser removes user's plainfile, succeeds
    @Test
    public void successRootRemovePlainFile() {

        Directory workingDirectory = changeWorkingDirectory(tokenRu, "/home/evaristo");
        final String fileName = "plain";
        DeleteFileService service = new DeleteFileService(tokenRu, fileName);
        service.execute();

        assertFalse("RootUser failed to remove plainFile: " + fileName, workingDirectory.hasFile(fileName,ru));
    }

    // 2. owner removes their plainfile, succeeds
    @Test
    public void successRemovePlainFile() {

        Directory workingDirectory = changeWorkingDirectory(token, "/home/evaristo");
        final String fileName = "plain";
        DeleteFileService service = new DeleteFileService(token, fileName);
        service.execute();

        assertFalse("Invalid plainFile removal: "+fileName, workingDirectory.hasFile(fileName,ru));

    }

    // 3. owner removes their app, succeeds
    @Test
    public void successRemoveApp() {

        Directory workingDirectory = changeWorkingDirectory(token, "/home/evaristo");
        final String fileName = "app";
        DeleteFileService service = new DeleteFileService(token, fileName);
        service.execute();

        assertFalse("Invalid app removal: "+fileName, workingDirectory.hasFile(fileName,ru));
    }

    // 4. owner removes their link, succeeds
    @Test
    public void successRemoveLink() {

        Directory workingDirectory = changeWorkingDirectory(token, "/home/evaristo");
        final String fileName = "link";
        DeleteFileService service = new DeleteFileService(token, fileName);
        service.execute();

        assertFalse("Invalid link removal: "+fileName, workingDirectory.hasFile(fileName,ru));
    }

    // 5. owner removes empty directory, succeeds
    @Test
    public void successRemoveEmptyDirectory() {

        Directory workingDirectory = changeWorkingDirectory(token, "/home/evaristo");
        final String directoryName = "empty";
        DeleteFileService service = new DeleteFileService(token, directoryName);
        service.execute();

        assertFalse("Invalid empty directory removal: "+directoryName, workingDirectory.hasFile(directoryName,ru));
    }

    // 6. owner removes directory with sub files, succeeds
    @Test
    public void successRemoveDirectory() {

        Directory workingDirectory = changeWorkingDirectory(token, "/home/evaristo");

        final String fileName = "one";
        DeleteFileService service = new DeleteFileService(token, fileName);
        service.execute();

        assertFalse("Invalid one Directory removal: "+fileName, workingDirectory.hasFile(fileName,ru));

    }

    // 7. user removes "." file, fails
    @Test(expected = IllegalRemovalException.class)
    public void removeIllegalFile() {
        final String fileName = ".";
        DeleteFileService service = new DeleteFileService(token, fileName);
        service.execute();
    }

    // 8. user tries to remove a non existing file, fails
    @Test(expected = EntryUnknownException.class)
    public void removeUnknownFile() {
        final String fileName = "opopopopopoppopop";
        DeleteFileService service = new DeleteFileService(token, fileName);
        service.execute();
    }

    // 9. user removes another user home directory, fails
    @Test(expected = AccessDeniedException.class)
    public void removeOtherUserHomeDirectory() {

        Directory workingDirectory = changeWorkingDirectory(token, "/home");
        final String fileName = "tony";
        DeleteFileService service = new DeleteFileService(token, fileName);
        service.execute();
    }

    // 10. owner removes his own home directory, fails
    @Test(expected = IllegalRemovalException.class)
    public void removeOwnUserHomeDirectory() {

        Directory workingDirectory = changeWorkingDirectory(token, "/home");
        final String fileName = "evaristo";
        DeleteFileService service = new DeleteFileService(token, fileName);
        service.execute();
    }

    // 11. user removes other user plain file, fails
    @Test(expected = AccessDeniedException.class)
    public void removeOtherUserPlainFile() {

        Directory workingDirectory = changeWorkingDirectory(token, "/home/tony");
        final String fileName = "disto";
        DeleteFileService service = new DeleteFileService(token, fileName);
        service.execute();
    }

    // 12 .user removes other user directory, fails
   @Test(expected = AccessDeniedException.class)
    public void removeOtherUserDirectory() {

        Directory workingDirectory = changeWorkingDirectory(token, "/home/tony/carreira");
        final String fileName = "c1";
        DeleteFileService service = new DeleteFileService(token, fileName);
        service.execute();
    }

    // 13. user removes directory created by root, fails
    @Test(expected = AccessDeniedException.class)
    public void removeDirectoryByRootUser() {

        Directory workingDirectory = changeWorkingDirectory(token, "/home/tony");
        final String fileName = "carreira";
        DeleteFileService service = new DeleteFileService(token, fileName);
        service.execute();
    }

    @Test (expected = SessionExpiredException.class)
    public void removeWithExpiredSession() {
        DateTime now = new DateTime();
        Login l = FileSystem.getInstance().getLogin();
        Directory workingDirectory = changeWorkingDirectory(token, "/home/evaristo");

        l.getSession(token).setExpiryDate(now.minusHours(3));      

        final String fileName = "plain";
        DeleteFileService service = new DeleteFileService(token, fileName);
        service.execute();

    }

    @Test (expected = SessionUnknownException.class)
    public void removeWithNoSession() {
        Login l = FileSystem.getInstance().getLogin();
        Directory workingDirectory = changeWorkingDirectory(token, "/home/evaristo");
        l.getSession(token).remove();

        final String fileName = "plain";
        DeleteFileService service = new DeleteFileService(token, fileName);
        service.execute();
    }

}
