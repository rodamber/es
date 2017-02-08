package pt.tecnico.mydrive.service;

import static org.junit.Assert.assertEquals;
import org.joda.time.DateTime;

import org.junit.Test;
import org.junit.runner.RunWith;

import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

import pt.tecnico.mydrive.domain.*;
import pt.tecnico.mydrive.exception.*;

@RunWith(JMockit.class)
public class EnvironmentLinksTest extends AbstractServiceTest {
    User john;
    User peter;
    long token;
    long token2;
    EnvironmentLinksService service;
    Login login;

    PlainFile pf;
    Link link;
    Link link2;
    App app;

    private File getFile(long token, String filename) throws EntryUnknownException {
        Directory cwd = FileSystem.getInstance().getLogin().getSession(token).getCwd();
        Directory root_dir = FileSystem.getInstance().getRootDirectory();

        if(filename.charAt(0) == '/')
            return root_dir.getFile(filename, john);
        else
            return cwd.getFile(filename, john);
    }

    protected void populate() {
        FileSystem fs = FileSystem.getInstance();
        john = new User(fs, "John", "John", "john_password", null, null);
        peter = new User(fs, "Peter", "Peter", "peter_password", null, null);
        token = fs.login(john.getUsername(), "john_password");
        token2 = fs.login(peter.getUsername(), "peter_password");

        login = fs.getLogin();
        new EnvVar(login, "USER", "John");

        Directory johnHome = john.getHomeDirectory();

        pf = new PlainFile(fs, johnHome, "PlainFile", john, "/home/John/App");
        link = new Link(fs, johnHome, "Link", john, "/home/John/PlainFile");
        link2 = new Link(fs, johnHome, "Link2", john, "/home/John/Link");
        app = new App(fs, johnHome, "App", john, "pt.tecnico.mydrive.service.EnvironmentLinksTest.test");
    }

    public static void test(String[] args){
        System.out.println("Testing");
    }

    //1. Owner writes a content of his link, succeeds
    @Test
    public void writeLinkSuccess() {
        new MockUp<EnvironmentLinksService>() {
            @Mock
            void dispatch() {
                pf.write("new content", john);
            }
        };

        String content = "new content";
        service = new EnvironmentLinksService(token, "/home/$USER/Link", "write", content);
        service.execute();

        String fileContent = getFile(token, "/home/John/PlainFile").read(john);
        assertEquals("Environment link not written or incorrectly written", content, fileContent);
    }

    //2. Owner reads his absolute link, succeeds
    @Test
    public void readAbsoluteLinkSuccess() {
        new MockUp<EnvironmentLinksService>() {
            @Mock
            String result() {
               return pf.read(john);
            }
        };

        service = new EnvironmentLinksService(token, "/home/$USER/Link", "read", null);
        service.execute();
        String contents = service.result();

        String fileContent = getFile(token, "/home/John/PlainFile").read(john);
        assertEquals("Content not correct " + fileContent + " got: " + contents, fileContent, contents);
    }

    //3. Owner reads his relative link, succeeds
    @Test
    public void readRelativeLinkSuccess() {
        new MockUp<EnvironmentLinksService>() {
            @Mock
            String result() {
                return pf.read(john);
            }
        };

        service = new EnvironmentLinksService(token, "home/$USER/Link", "read", null);
        service.execute();
        String contents = service.result();

        String fileContent = getFile(token, "/home/John/PlainFile").read(john);
        assertEquals("Content should be: " + fileContent + " got: " + contents, fileContent, contents);
    }

    //4. Owner reads a link to a link, succeeds
    @Test
    public void readLinkToLinkSuccess() {
        new MockUp<EnvironmentLinksService>() {
            @Mock
            String result() {
                return link.read(john);
            }
        };

        service = new EnvironmentLinksService(token, "/home/$USER/Link2", "read", null);
        service.execute();
        String contents = service.result();

        String fileContent = getFile(token, "/home/John/Link").read(john);
        assertEquals("Content should be: " + fileContent + " got: " + contents, fileContent, contents);
    }

    //5. Owner executes his link, succeeds
    @Test
    public void executeLinkSuccess() {
        new MockUp<EnvironmentLinksService>() {
            @Mock
            void dispatch() {
                pf.execute(john, null);
            }
        };

        service = new EnvironmentLinksService(token, "/home/$USER/Link", "execute", null);
        service.execute();
    }

    //6. User accesses unknown link, fails
    @Test(expected = LinkUnknownException.class)
    public void linkUnknownFails() {
        final String unknown = "/home/$TEST/Link";

        new MockUp<EnvironmentLinksService>() {
            @Mock
            void dispatch() {
                throw new LinkUnknownException(unknown);
            }
        };

        service = new EnvironmentLinksService(token, unknown, "read", null);
        service.execute();
    }

    //7. User accesses other user's link, fails
    @Test(expected = AccessDeniedException.class)
    public void linkNoPermissionsFails() {
        new MockUp<EnvironmentLinksService>() {
            @Mock
            void dispatch() {
                throw new AccessDeniedException();
            }
        };

        service = new EnvironmentLinksService(token2, "/home/$USER/Link", "read", null); //read example
        service.execute();
    }

    //8. Owner without login accesses a link, fails
    @Test(expected = SessionUnknownException.class)
    public void sessionUnknownFails() {
        new MockUp<EnvironmentLinksService>() {
            @Mock
            void dispatch() {
                throw new SessionUnknownException();
            }
        };

        login.getSession(token).remove();
        service = new EnvironmentLinksService(token, "/home/$USER/Link", "read", null); //read example
        service.execute();
    }

    //9. Owner with expired session accesses a link, fails
    @Test(expected = SessionExpiredException.class)
    public void sessionExpiredFails() {
        new MockUp<EnvironmentLinksService>() {
            @Mock
            void dispatch() {
                throw new SessionExpiredException();
            }
        };

        DateTime now = new DateTime();
        login.getSession(token).setExpiryDate(now.minusHours(3));

        service = new EnvironmentLinksService(token, "/home/$USER/Link", "read", null); //read example
        service.execute();
    }

}
