package pt.tecnico.mydrive.service;
import java.util.*;
import java.io.*;
import org.joda.time.DateTime;

import static org.junit.Assert.*;

import org.junit.Test;

import pt.tecnico.mydrive.domain.*;
import pt.tecnico.mydrive.exception.*;

public class CreateFileTest extends AbstractServiceTest {

    FileSystem fs;
    Login l;
    //users
    RootUser ru;
    User u1, u2;
    long u1Token, u2Token, ruToken;
    final String u1Name = "user1";
    final String u2Name = "user2";

    //files
    final String fName = "createdFile";
    final String f1Name = "testFile1";
    final String d1Name = "dir1";
    final String d2Name = "dir2";
    String bigFileName = "";

    protected void populate() {
        fs = FileSystem.getInstance();
        l = fs.getLogin();

        ru = fs.getRootUser();
        u1 = new User(fs, u1Name, u1Name, u1Name+"_password", null, null);
        u2 = new User(fs, u2Name, u2Name, u2Name+"_password", null, null);

        Directory dir1 = new Directory(fs, u1.getHomeDirectory(), d1Name, u1);
        Directory dir2 = new Directory(fs, u2.getHomeDirectory(), d2Name, u2);
        u2.getHomeDirectory().getFile(d2Name, u2).getPermissions().setPermissions(u2, "rwxdrwxd");

        //name, owner, content
        PlainFile plainfile = new PlainFile(fs, dir1, f1Name, u1);

        ruToken = fs.login(ru.getUsername(), "***");

        for (int i = 0; i < 1050; i++) {
            bigFileName += "a";
        }

        u1Token = fs.login(u1.getUsername(), u1Name+"_password");
        u2Token = fs.login(u2.getUsername(), u2Name+"_password");

    }

    private String makeHomePath(User u) {
        return "/home/" + u.getHomeDirectory().getName();
    }

    //1. Root creates a plainfile (even with no permissions), succeeds
    @Test
    public void rootCreatePlainFileSuccess() {
        Directory workingDirectory = l.sessionChangeWorkingDirectory(ruToken, makeHomePath(u1) + "/" + d1Name);

        CreateFileService service = new CreateFileService(ruToken, fName, "PlainFile");
        service.execute();

        assertTrue("Invalid plainFile creation: " + fName, workingDirectory.hasFile(fName, u1));
    }

    //2. Owner of directory creates a plainfile, succeeds
    @Test
    public void ownerCreatePlainFileSuccess() {
        Directory workingDirectory = l.sessionChangeWorkingDirectory(u1Token, makeHomePath(u1) + "/" + d1Name);

        CreateFileService service = new CreateFileService(u1Token, fName, "PlainFile");
        service.execute();

        assertTrue("Invalid app creation: " + fName, workingDirectory.hasFile(fName, u1));
    }

    //3. Owner of directory creates an empty app, succeeds
    @Test
    public void ownerCreateAppSuccess() {
        Directory workingDirectory = l.sessionChangeWorkingDirectory(u1Token, makeHomePath(u1) + "/" + d1Name);

        CreateFileService service = new CreateFileService(u1Token, fName, "App");
        service.execute();

        assertTrue("Invalid app creation: " + fName, workingDirectory.hasFile(fName, u1));
    }

    //4. Owner of directory creates an app with content (package.class.name), succeeds
    @Test
    public void ownerCreateAppContentPCNSuccess() {
        Directory workingDirectory = l.sessionChangeWorkingDirectory(u1Token, makeHomePath(u1) + "/" + d1Name);

        CreateFileService service = new CreateFileService(u1Token, fName, "App", "package.class.name");
        service.execute();

        assertTrue("Invalid app creation: " + fName, workingDirectory.hasFile(fName, u1));
    }

    //5. Owner of directory creates an app with content (package.class), succeeds
    @Test
    public void ownerCreateAppContentPCSuccess() {
        Directory workingDirectory = l.sessionChangeWorkingDirectory(u1Token, makeHomePath(u1) + "/" + d1Name);

        CreateFileService service = new CreateFileService(u1Token, fName, "App", "package.class");
        service.execute();

        assertTrue("Invalid app creation: " + fName, workingDirectory.hasFile(fName, u1));
    }

    //6. Owner of directory creates an app with content (package.package.package.class.name), succeeds
    @Test
    public void ownerCreateAppContentPPPCNSuccess() {
        Directory workingDirectory = l.sessionChangeWorkingDirectory(u1Token, makeHomePath(u1) + "/" + d1Name);

        CreateFileService service = new CreateFileService(u1Token, fName, "App", "package.package.package.class.name");
        service.execute();

        assertTrue("Invalid app creation: " + fName, workingDirectory.hasFile(fName, u1));
    }

    //7. Owner of directory creates an app with content (package), succeeds
    @Test
    public void ownerCreateAppContentPSuccess() {
        Directory workingDirectory = l.sessionChangeWorkingDirectory(u1Token, makeHomePath(u1) + "/" + d1Name);

        CreateFileService service = new CreateFileService(u1Token, fName, "App", "package");
        service.execute();

        assertTrue("Invalid app creation: " + fName, workingDirectory.hasFile(fName, u1));
    }

    //8. Owner of directory creates an app with content (package..class.name), fails
    @Test(expected = InvalidAppContentException.class)
    public void ownerCreateAppContentTwoDotsFail() {
        Directory workingDirectory = l.sessionChangeWorkingDirectory(u1Token, makeHomePath(u1) + "/" + d1Name);

        CreateFileService service = new CreateFileService(u1Token, fName, "App", "package..class.name");
        service.execute();
    }

    //9. Owner of directory creates an app with content (.package.class.name), fails
    @Test(expected = InvalidAppContentException.class)
    public void ownerCreateAppContentDotBeforeFail() {
        Directory workingDirectory = l.sessionChangeWorkingDirectory(u1Token, makeHomePath(u1) + "/" + d1Name);

        CreateFileService service = new CreateFileService(u1Token, fName, "App", ".package.class.name");
        service.execute();
    }

    //10. Owner of directory creates an app with content (package.class.name.), fails
    @Test(expected = InvalidAppContentException.class)
    public void ownerCreateAppContentDotAfterFail() {
        Directory workingDirectory = l.sessionChangeWorkingDirectory(u1Token, makeHomePath(u1) + "/" + d1Name);

        CreateFileService service = new CreateFileService(u1Token, fName, "App", "package.class.name.");
        service.execute();
    }

    //11. User creates a PlainFile with a name that already exists, fails
    @Test(expected = EntryExistsException.class)
    public void userCreatePlainFileFail() {
        Directory workingDirectory = l.sessionChangeWorkingDirectory(u1Token, makeHomePath(u1) + "/" + d1Name);

        CreateFileService service = new CreateFileService(u1Token, f1Name, "PlainFile");
        service.execute();
    }

    //12. User (not owner of directory) without permissions creates a PlainFile, fails
    @Test(expected = AccessDeniedException.class)
    public void userNoPermissionsCreatePlainFileFail() {

        u1.getHomeDirectory().getPermissions().setPermissions(u1, "rwxdr-x-");
        u1.getHomeDirectory().getFile(d1Name, u1).getPermissions().setPermissions(u1, "rwxdr-x-");
        Directory workingDirectory = l.sessionChangeWorkingDirectory(u2Token, makeHomePath(u1) + "/" + d1Name);

        CreateFileService service = new CreateFileService(u2Token, fName, "PlainFile");
        service.execute();
    }

    //13. User (not owner of directory) with permissions creates a Directory, succeeds
    @Test
    public void userWithPermissionsCreateDirectorySuccess() {
        u2.getHomeDirectory().getPermissions().setPermissions(u2, "rwxdrwxd");

        Directory workingDirectory = l.sessionChangeWorkingDirectory(u1Token, makeHomePath(u2) + "/" + d2Name);

        CreateFileService service = new CreateFileService(u1Token, fName, "Directory");
        service.execute();

        assertTrue("Invalid directory creation: " + fName, workingDirectory.hasFile(fName, u1));
    }

    //14. Owner of directory without permissions creates a PlainFile, fails
    @Test(expected = AccessDeniedException.class)
    public void ownerNoPermissionsCreatePlainFileFail() {
        String dirPath = makeHomePath(u1) + "/" + d1Name;
        u1.getHomeDirectory().getFile(d1Name, u1).getPermissions().setPermissions(u1, "r-xd----");
        Directory workingDirectory = l.sessionChangeWorkingDirectory(u1Token, makeHomePath(u1) + "/" + d1Name);

        CreateFileService service = new CreateFileService(u1Token, fName, "PlainFile");
        service.execute();
    }

    //15. Owner of directory creates a link with absolute path, succeeds
    @Test
    public void ownerCreateLinkAbsoluteSuccess() {
        Directory workingDirectory = l.sessionChangeWorkingDirectory(u1Token, makeHomePath(u1) + "/" + d1Name);

        CreateFileService service = new CreateFileService(u1Token, fName, "Link", "/home");
        service.execute();

        assertTrue("Invalid link creation: " + fName, workingDirectory.hasFile(fName, u1));
    }

    //16. Owner of directory creates a link with relative path, succeeds
    @Test
    public void ownerCreateLinkRelativeSuccess() {
        Directory workingDirectory = l.sessionChangeWorkingDirectory(u1Token, makeHomePath(u1));

        CreateFileService service = new CreateFileService(u1Token, fName, "Link", "dir1/testFile1");
        service.execute();

        assertTrue("Invalid link creation: " + fName, workingDirectory.hasFile(fName, u1));
    }

    //17. Owner of directory creates a link with relative path to unexisting file, fails
    @Test(expected = InvalidLinkContentException.class)
    public void ownerCreateLinkRelativeNoExistingFileFail() {
        Directory workingDirectory = l.sessionChangeWorkingDirectory(u1Token, makeHomePath(u1) + "/" + d1Name);

        CreateFileService service = new CreateFileService(u1Token, fName, "Link", "x");
        service.execute();
    }

    //18. Owner of directory creates a link with absolute path to unexisting file, fails
    @Test(expected = InvalidLinkContentException.class)
    public void ownerCreateLinkAbsoluteNoExistingFileFail() {
        Directory workingDirectory = l.sessionChangeWorkingDirectory(u1Token, makeHomePath(u1) + "/" + d1Name);

        CreateFileService service = new CreateFileService(u1Token, fName, "Link", "/x");
        service.execute();
    }

    //19. Owner of directory creates a link with wrong content, fails
    @Test(expected = InvalidLinkContentException.class)
    public void ownerCreateLinkNoPathContentFail() {
        Directory workingDirectory = l.sessionChangeWorkingDirectory(u1Token, makeHomePath(u1) + "/" + d1Name);

        CreateFileService service = new CreateFileService(u1Token, fName, "Link", "123");
        service.execute();
    }

    //20. Owner of directory creates a link with path to itself, fails
    @Test(expected = InvalidLinkContentException.class)
    public void ownerCreateLinkPathToItselfFail() {
        Directory workingDirectory = l.sessionChangeWorkingDirectory(u1Token, makeHomePath(u1) + "/" + d1Name);

        CreateFileService service = new CreateFileService(u1Token, fName, "Link", fName);
        service.execute();
    }

    //21. Owner of directory creates a file with invalid name, fails
    @Test(expected = InvalidFileNameException.class)
    public void ownerCreateFileInvalidName() {
        Directory workingDirectory = l.sessionChangeWorkingDirectory(u1Token, makeHomePath(u1) + "/" + d1Name);

        CreateFileService service = new CreateFileService(u1Token, "/", "PlainFile");
        service.execute();
    }

    //22. Owner of directory creates a file and path size exceeds 1024 chars, fails
    @Test(expected = AbsolutePathSizeLimitExceeded.class)
    public void ownerCreateFilePathSizeLimitExceeded() {
        Directory workingDirectory = l.sessionChangeWorkingDirectory(u1Token, makeHomePath(u1) + "/" + d1Name);

        CreateFileService service = new CreateFileService(u1Token, bigFileName, "PlainFile");
        service.execute();
    }

    //23. Owner with expired session tries to create a file, exception thrown
    @Test(expected = SessionExpiredException.class)
    public void expiredSessionCreate() {
        DateTime now = new DateTime();

        l.sessionChangeWorkingDirectory(u1Token, makeHomePath(u1) + "/" + d1Name);

        l.getSession(u1Token).setExpiryDate(now.minusHours(3));

        CreateFileService service = new CreateFileService(u1Token, fName, "PlainFile");
        service.execute();
    }

    //24. Owner without login tries to read a file, exception thrown
    @Test(expected = SessionUnknownException.class)
    public void noSessionCreate() {
        l.sessionChangeWorkingDirectory(u1Token, makeHomePath(u1) + "/" + d1Name);
        l.getSession(u1Token).remove();

        CreateFileService service = new CreateFileService(u1Token, fName, "PlainFile");
        service.execute();
    }

}
