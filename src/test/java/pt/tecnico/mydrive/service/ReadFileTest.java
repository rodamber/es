package pt.tecnico.mydrive.service;

import org.joda.time.DateTime;

//junit
import org.junit.Test;
import static org.junit.Assert.assertEquals;

//service
import pt.tecnico.mydrive.service.ReadFileService;

//domain
import pt.tecnico.mydrive.domain.FileSystem;
import pt.tecnico.mydrive.domain.Login;
import pt.tecnico.mydrive.domain.RootUser;
import pt.tecnico.mydrive.domain.User;
import pt.tecnico.mydrive.domain.Directory;
import pt.tecnico.mydrive.domain.PlainFile;
import pt.tecnico.mydrive.domain.App;
import pt.tecnico.mydrive.domain.Link;

//exceptions
import pt.tecnico.mydrive.exception.IsNotPlainFileException;
import pt.tecnico.mydrive.exception.AccessDeniedException;
import pt.tecnico.mydrive.exception.EntryUnknownException;
import pt.tecnico.mydrive.exception.SessionUnknownException;
import pt.tecnico.mydrive.exception.SessionExpiredException;

public class ReadFileTest extends AbstractServiceTest {
    FileSystem fs;
    Login l;
    //users
    RootUser ru;
    User u1, u2;
    long u1Token, u2Token, ruToken;
    final String u1Name = "Ana";
    final String u2Name = "Ivo";
    //files
    final String f1Name = "lorem";
    final String f2Name = "ipsum";
    final String f3Name = "dolor";
    final String f4Name = "sit"; //Link to f3
    final String f5Name = "amet"; //Link to f4
    final String f6Name = "adipiscing"; //PlainFile inside a directory
    final String f7Name = "elit"; //Link to f6
    final String f1Content = f1Name + " test"; //PF
    final String f2Content = f2Name + ".test"; //App
    final String f3Content = f3Name + " test"; //PF
    final String f6Content = f6Name + " test"; //PF
    final String dName = "sed";

    protected void populate() {
        //------Create FS
        fs = FileSystem.getInstance();
        l = fs.getLogin();

        ru = fs.getRootUser();
        u1 = new User(fs, u1Name, u1Name, u1Name+"_password", null, null);
        u2 = new User(fs, u2Name, u2Name, u2Name+"_password", null, null);

        //------U1                               //name, owner, content
        new PlainFile(fs, u1.getHomeDirectory(), f1Name, u1, f1Content);
        new App(fs, u1.getHomeDirectory(), f2Name, u1, f2Content);

        //------U2
        PlainFile f3 = new PlainFile(fs, u2.getHomeDirectory(), f3Name, u2, f3Content);

        Directory dir = new Directory(fs, u2.getHomeDirectory(), dName, u2);

        //------U2 - Links
        Link f4 = new Link(fs, u2.getHomeDirectory(), f4Name, u2, f3.getAbsolutePath());

        new Link(fs, u2.getHomeDirectory(), f5Name, u2, f4.getAbsolutePath());

        new PlainFile(fs, dir, f6Name, u2, f6Content);

        Link f7 = new Link(fs, u2.getHomeDirectory(), f7Name, u2, dName + "/" + f6Name);

        //------Logins
        ruToken = fs.login(ru.getUsername(), "***");
        u1Token = fs.login(u1.getUsername(), u1Name+"_password");
        u2Token = fs.login(u2.getUsername(), u2Name+"_password");
    }

    private String makeHomePath(User u) {
        return "/home/" + u.getHomeDirectory().getName();
    }

    //1. file owner reads their plainfile, succeeds
    @Test
    public void ownerPlainFileSuccess() {
        l.sessionChangeWorkingDirectory(u1Token, makeHomePath(u1));

        ReadFileService service = new ReadFileService(u1Token, f1Name);
        service.execute();
        String content = service.result();

        assertEquals("Content should be: " + f1Content + " got: " + content, f1Content, content);
    }

    //2. file owner reads their app, succeeds
    @Test
    public void ownerAppSuccess() {
        l.sessionChangeWorkingDirectory(u1Token, makeHomePath(u1));

        ReadFileService service = new ReadFileService(u1Token, f2Name);
        service.execute();
        String content = service.result();

        assertEquals("Content should be: " + f2Content + " got: " + content, f2Content, content);
    }

    //3. file owner reads their link (absolute path), succeeds
    @Test
    public void ownerLinkAbsSuccess() {
        l.sessionChangeWorkingDirectory(u2Token, makeHomePath(u2));

        ReadFileService service = new ReadFileService(u2Token, f4Name);
        service.execute();
        String content = service.result();

        assertEquals("Content should be: " + f3Content + " got: " + content, f3Content, content);
    }

    //4. file owner reads their link (relative path), succeeds
    @Test
    public void ownerLinkRelSuccess() {
        l.sessionChangeWorkingDirectory(u2Token, makeHomePath(u2));

        ReadFileService service = new ReadFileService(u2Token, f7Name);
        service.execute();
        String content = service.result();

        assertEquals("Content should be: " + f6Content + " got: " + content, f6Content, content);
    }

    //5. file owner reads their link, which is also a link, succeeds
    @Test
    public void ownerLinkWithLinkSuccess() {
        l.sessionChangeWorkingDirectory(u2Token, makeHomePath(u2));

        ReadFileService service = new ReadFileService(u2Token, f5Name);
        service.execute();
        String content = service.result();

        assertEquals("Content should be: " + f3Content + " got: " + content, f3Content, content);
    }

    //6. root reads other user's file, succeeds
    @Test
    public void rootSuccess() {
        l.sessionChangeWorkingDirectory(ruToken, makeHomePath(u1));

        ReadFileService service = new ReadFileService(ruToken, f2Name);
        service.execute();
        String content = service.result();

        assertEquals("Content should be: " + f2Content + " got: " + content, f2Content, content);
    }

    //7. user reads other user's file, succeeds
    @Test
    public void userSuccess() {

        u1.getHomeDirectory().getPermissions().setPermissions(u1, "rwxdrwxd");
        u1.getHomeDirectory().getFile(f2Name, u1).getPermissions().setPermissions(u1, "rwxdrwxd");

        l.sessionChangeWorkingDirectory(u2Token, makeHomePath(u1));

        ReadFileService service = new ReadFileService(u2Token, f2Name);
        service.execute();
        String content = service.result();

        assertEquals("Content should be: " + f2Content + " got: " + content, f2Content, content);
    }


    //8. user tries to read a directory, exception thrown
    @Test(expected = IsNotPlainFileException.class)
    public void readDirectory() {
        l.sessionChangeWorkingDirectory(u2Token, makeHomePath(u2));

        ReadFileService service = new ReadFileService(u2Token, dName);
        service.execute();
    }

    //9. user tries to read a non existing file, exception thrown
    @Test(expected = EntryUnknownException.class)
    public void readUnknown() {
        l.sessionChangeWorkingDirectory(u1Token, makeHomePath(u1));

        ReadFileService service = new ReadFileService(u1Token, "noFile");
        service.execute();
    }

    //10. user without permission tries to read a file, exception thrown
    @Test(expected = AccessDeniedException.class)
    public void noPermissionRead() {

        u2.getHomeDirectory().getPermissions().setPermissions(u2, "rwxdrwxd");

        l.sessionChangeWorkingDirectory(u1Token, makeHomePath(u2));

        ReadFileService service = new ReadFileService(u1Token, f3Name);
        service.execute();
    }

    //11. owner without permission tries to read a file, exception thrown
    @Test(expected = AccessDeniedException.class)
    public void ownerNoPermissionRead() {

        u1.getHomeDirectory().getFile(f1Name, u1).getPermissions().setPermissions(u1, "--------");

        l.sessionChangeWorkingDirectory(u1Token, makeHomePath(u1));

        ReadFileService service = new ReadFileService(u1Token, f1Name);
        service.execute();
    }

    //12. owner with expired session tries to read a file, exception thrown
    @Test (expected = SessionExpiredException.class)
    public void expiredSessionRead() {
        DateTime now = new DateTime();

        l.sessionChangeWorkingDirectory(u1Token, makeHomePath(u1));

        l.getSession(u1Token).setExpiryDate(now.minusHours(3));

        ReadFileService service = new ReadFileService(u1Token, f1Name);
        service.execute();
    }

    //13. owner without login tries to read a file, exception thrown
    @Test (expected = SessionUnknownException.class)
    public void noSessionRead() {
        l.sessionChangeWorkingDirectory(u1Token, makeHomePath(u1));
        l.getSession(u1Token).remove();

        ReadFileService service = new ReadFileService(u1Token, f1Name);
        service.execute();
    }


}
