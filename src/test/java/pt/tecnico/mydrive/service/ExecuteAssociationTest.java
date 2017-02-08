package pt.tecnico.mydrive.service;

import java.util.Arrays;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;

import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

import pt.tecnico.mydrive.domain.*;
import pt.tecnico.mydrive.exception.*;

@RunWith(JMockit.class)
public class ExecuteAssociationTest extends AbstractServiceTest {
    Login l;
    
    User u;
    long ownerTok;
    long rootTok;
    long otherTok;
    
    App appC;
    String fileCPath = "/home/peter/hello.c";
        
    ExecuteAssociationService service;
    
    protected void populate() {
        FileSystem fs = FileSystem.getInstance();
        l = fs.getLogin();
        
        User root = fs.getRootUser();        
        u = new User(fs, "peter", "peter", "peter_password", null, null);
        User other = new User(fs, "alfred", "alfred", "alfred_password", null, null);
        
        rootTok = fs.login(root.getUsername(), "***");
        ownerTok = fs.login(u.getUsername(), "peter_password");
        otherTok = fs.login(other.getUsername(), "alfred_password");

        Directory peterHome = u.getHomeDirectory();
        new PlainFile(fs, peterHome, "hello.c", u);
        new PlainFile(fs, peterHome, "hello.java", u);
        appC = new App(fs, peterHome, "AppC", u, "pt.tecnico.mydrive.service.ExecuteAssociationTest.test");
        new FileExtension(u, "c", appC);
               
    }
    
    public static void test(String[] args){
        System.out.println("*** Executing app for c extension ***");
    }
    
    // 1. Root executes a user file association (succeeds)
    @Test
    public void rootExecuteAssociation() {
        new MockUp<ExecuteAssociationService>() {
            @Mock
            void dispatch(){
                User user = l.sessionGetUser(rootTok);
                appC.execute(user, null);
            }
        };

        service = new ExecuteAssociationService(rootTok, fileCPath);
        service.execute();
    }
    
    // 2. Owner executes a user file association (succeeds)
    @Test
    public void ownerExecuteAssociation() {
        
        new MockUp<ExecuteAssociationService>() {
            @Mock
            void dispatch(){
                User user = l.sessionGetUser(ownerTok);
                appC.execute(user, null);
            }
        };

        service = new ExecuteAssociationService(ownerTok, fileCPath);
        service.execute();
    }
    
    // 3. Other user executes a user file association (fails)
    @Test(expected = AccessDeniedException.class)
    public void otherExecuteAssociation() {
        new MockUp<ExecuteAssociationService>() {
            @Mock
            void dispatch() throws MyDriveException{
                throw new AccessDeniedException();
            }
        };

        service = new ExecuteAssociationService(otherTok, fileCPath);
        service.execute();
    }
    
    // 4. User executes an unknown file association (fails)
    @Test(expected = EntryUnknownException.class)
    public void executeUnknownFile() { 
        new MockUp<ExecuteAssociationService>() {
            @Mock
            void dispatch() throws MyDriveException{
                throw new EntryUnknownException("bye.c");
            }
        };

        service = new ExecuteAssociationService(ownerTok, "/home/peter/bye.c");
        service.execute();
    }
    
    // 5. User executes an unknown file association (fails)
    @Test(expected = ExtensionUnknownException.class)
    public void executeUnknownExtension() {
        
        new MockUp<ExecuteAssociationService>() {
            @Mock
            void dispatch() throws MyDriveException {
                throw new ExtensionUnknownException("java"); 
            }
        };

        service = new ExecuteAssociationService(ownerTok, "/home/peter/hello.java");
        service.execute();
    }

    // 6. User with an expired session tries to execute file association (fails)
    @Test(expected = SessionExpiredException.class)
    public void executeAssociationExpiredSession() {
        new MockUp<ExecuteAssociationService>() {
            @Mock
            void dispatch() throws MyDriveException{
                throw new SessionExpiredException();
            }
        };
        
        DateTime now = new DateTime();
        
        l.getSession(ownerTok).setExpiryDate(now.minusHours(3));

        service = new ExecuteAssociationService(ownerTok, fileCPath);
        service.execute();
    }
    
    // 7. User with a unknown session tries to execute file association (fails)
    @Test(expected = SessionUnknownException.class)
    public void executeAssociationNoSession() {
        new MockUp<ExecuteAssociationService>() {
            @Mock
            void dispatch() throws MyDriveException{
                throw new SessionUnknownException();
            }
        };
        
        l.getSession(ownerTok).remove();
        service = new ExecuteAssociationService(ownerTok, fileCPath);
        service.execute();
    }
    
}