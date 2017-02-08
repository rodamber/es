package pt.tecnico.mydrive.service;

import static org.junit.Assert.assertEquals;

import org.joda.time.DateTime;
import org.junit.Test;

import pt.tecnico.mydrive.domain.*;
import pt.tecnico.mydrive.exception.*;
import pt.tecnico.mydrive.service.ExecuteFileService;

public class ExecuteFileTest extends AbstractServiceTest{

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

        new PlainFile(fs, johnHome, "PlainFile", owner, "/home/John/App Ana");
        new PlainFile(fs, johnHome, "EmptyPlainFile", owner, "");
        new PlainFile(fs, johnHome, "InvalidPlainFile", owner, "/home/John/InvalidApp 1 2");
        new App(fs, johnHome, "App", owner, "pt.tecnico.mydrive.service.ExecuteFileTest.test");
        new App(fs, johnHome, "EmptyApp", owner, "");
        new App(fs, johnHome, "InvalidApp", owner, "pt.tecnico.mydrive.service.ExecuteFileTest.test2");
        new Link(fs, johnHome, "Link", owner, "/home/John/App");
    }
    
    public static void test(String[] args){
        if(args == null || args.length < 1)
    	    System.out.println("Testing");
    	else
    	    System.out.println("Testing" + args[0]);
    }

    // 1. Root tries to execute a Plain File with valid file structure (succeeds)
    @Test
    public void rootPlainFileExecute(){
        final String path = "/home/John/PlainFile";

        ExecuteFileService service = new ExecuteFileService(rootTok, path, null);
        service.execute();
    }

    // 2. Owner tries to execute a Plain File with valid file structure (succeeds)
    @Test
    public void ownerPlainFileExecute(){
        final String path = "/home/John/PlainFile";

        ExecuteFileService service = new ExecuteFileService(ownerTok, path, null);
        service.execute();
    }

    // 3. Other tries to execute a Plain File with valid file structure (fails)
    @Test(expected = pt.tecnico.mydrive.exception.AccessDeniedException.class)
    public void otherPlainFileExecute(){
        final String path = "/home/John/PlainFile";

        ExecuteFileService service = new ExecuteFileService(otherTok, path, null);
        service.execute();
    }

    // 4. Owner tries to execute a Plain File with an invalid App (succeeds)
    @Test(expected = java.lang.RuntimeException.class)
    public void plainFileExecuteInvalidApp(){
        final String path = "/home/John/InvalidPlainFile";

        ExecuteFileService service = new ExecuteFileService(ownerTok, path, null);
        service.execute();
    }

    // 5. Owner tries to execute an empty Plain File (fails)
    @Test(expected = java.lang.IllegalArgumentException.class)
    public void emptyPlainFileExecute(){
        final String path = "/home/John/EmptyPlainFile";

        ExecuteFileService service = new ExecuteFileService(rootTok, path, null);
        service.execute();
    }

    // 6. Root tries to execute a Link pointing to a valid executable file (succeeds)
    @Test
    public void rootLinkExecute(){
        final String path = "/home/John/Link";

        ExecuteFileService service = new ExecuteFileService(rootTok, path, null);
        service.execute();
    }

    // 7. Owner tries to execute a Link pointing to a valid executable file (succeeds)
    @Test
    public void ownerLinkExecute(){
        final String path = "/home/John/Link";

        ExecuteFileService service = new ExecuteFileService(ownerTok, path, null);
        service.execute();
    }

    // 8. Other tries to execute a Link pointing to a valid executable file (fails)
    @Test(expected = pt.tecnico.mydrive.exception.AccessDeniedException.class)
    public void otherLinkExecute(){
        final String path = "/home/John/Link";

        ExecuteFileService service = new ExecuteFileService(otherTok, path, null);
        service.execute();
    }

    // 9. Root tries to execute a valid App (succeeds)
    @Test
    public void rootAppExecute(){
        final String path = "/home/John/App";

        ExecuteFileService service = new ExecuteFileService(rootTok, path, null);
        service.execute();
    }

    // 10. Owner tries to execute a valid App (succeeds)
    @Test
    public void ownerAppExecute(){
        final String path = "/home/John/App";

        ExecuteFileService service = new ExecuteFileService(ownerTok, path, null);
        service.execute();
    }

    // 11. Other tries to execute a valid App (fails)
    @Test(expected = pt.tecnico.mydrive.exception.AccessDeniedException.class)
    public void otherAppExecute(){
        final String path = "/home/John/App";

        ExecuteFileService service = new ExecuteFileService(otherTok, path, null);
        service.execute();
    }

    // 12. User tries to execute an empty App (fails)
    @Test(expected = java.lang.RuntimeException.class)
    public void emptyAppExecute(){
        final String path = "/home/John/EmptyApp";

        ExecuteFileService service = new ExecuteFileService(ownerTok, path, null);
        service.execute();
    }

    // 13. User tries to execute a plain file with an expired token
    @Test(expected = SessionExpiredException.class)
    public void expiredSessionPlainFileExecute() {
        DateTime now = new DateTime();

        login.getSession(ownerTok).setExpiryDate(now.minusHours(3));

        final String path = "/home/John/PlainFile";

        ExecuteFileService service = new ExecuteFileService(ownerTok, path, null);
        service.execute();
    }

    // 14. User tries to execute a plain file with an expired token
    @Test(expected = SessionUnknownException.class)
    public void noSessionPlainFileExecute() {
        login.getSession(ownerTok).remove();

        final String path = "/home/John/PlainFile";

        ExecuteFileService service = new ExecuteFileService(ownerTok, path, null);
        service.execute();
    }

    // 15. User tries to execute a link with an expired token
    @Test(expected = SessionExpiredException.class)
    public void expiredSessionLinkExecute() {
        DateTime now = new DateTime();

        login.getSession(ownerTok).setExpiryDate(now.minusHours(3));

        final String path = "/home/John/Link";

        ExecuteFileService service = new ExecuteFileService(ownerTok, path, null);
        service.execute();
    }

    // 16. User tries to execute a link with an expired token
    @Test(expected = SessionUnknownException.class)
    public void noSessionLinkExecute() {
        login.getSession(ownerTok).remove();

        final String path = "/home/John/Link";

        ExecuteFileService service = new ExecuteFileService(ownerTok, path, null);
        service.execute();
    }

    // 17. User tries to execute an app with an expired token
    @Test(expected = SessionExpiredException.class)
    public void expiredSessionAppExecute() {
        DateTime now = new DateTime();

        login.getSession(ownerTok).setExpiryDate(now.minusHours(3));

        final String path = "/home/John/App";

        ExecuteFileService service = new ExecuteFileService(ownerTok, path, null);
        service.execute();
    }

    // 18. User tries to execute an app with an expired token
    @Test(expected = SessionUnknownException.class)
    public void noSessionAppExecute() {
        login.getSession(ownerTok).remove();

        final String path = "/home/John/App";

        ExecuteFileService service = new ExecuteFileService(ownerTok, path, null);
        service.execute();
    }

}
