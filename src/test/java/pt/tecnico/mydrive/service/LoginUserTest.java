package pt.tecnico.mydrive.service;

import static org.junit.Assert.*;

import org.joda.time.DateTime;

//junit
import org.junit.Test;

//domain
import pt.tecnico.mydrive.domain.FileSystem;
import pt.tecnico.mydrive.domain.Login;
import pt.tecnico.mydrive.domain.Permissions;
import pt.tecnico.mydrive.domain.User;

//exceptions
import pt.tecnico.mydrive.exception.WrongPasswordException;
import pt.tecnico.mydrive.exception.NoPasswordException;
import pt.tecnico.mydrive.exception.UserUnknownException;

public class LoginUserTest extends AbstractServiceTest {
    FileSystem fs;
    User u;
    Login l;

    final String uName = "Etelvina";
    final String uUsername = "etelvina95";
    final String uPassword = "PassWord_Compl1cada";

    long uToken;
    protected void populate() {
        fs = FileSystem.getInstance();
        l = fs.getLogin();

        new User(fs, uUsername, uName, uPassword, null, null);
    }

    //1. filesystem logins with valid username and valid password, succeeds
    @Test
    public void validLogin() {
        LoginUserService service = new LoginUserService(uUsername, uPassword);
        service.execute();
        long token = service.result();

        assertTrue("Valid token", token!=0);
    }

    //2. filesystem logins with valid username and invalid password, fails
    @Test(expected = WrongPasswordException.class)
    public void invalidPasswordLogin() {
        LoginUserService service = new LoginUserService(uUsername, uPassword+"_parafalhar");
        service.execute();
    }

    //3.  logins with an invalid username, fails
    @Test(expected = UserUnknownException.class)
    public void invalidUsernameLogin() {
        LoginUserService service = new LoginUserService(uUsername+"_parafalhar", uPassword);
        service.execute();
    }

    //4.  logins with null username, fails
    @Test(expected = UserUnknownException.class)
    public void nullUsernameLogin() {
        LoginUserService service = new LoginUserService(null, uPassword);
        service.execute();
    }

    //5.  logins with null password, fails
    @Test(expected = NoPasswordException.class)
    public void nullPasswordLogin() {
        LoginUserService service = new LoginUserService(uUsername, null);
        service.execute();
    }
    
    //6.  logins with root, succeeds
    @Test
    public void rootLogin() {
        LoginUserService service = new LoginUserService("root", "***");
        service.execute();
        long token = service.result();
        
        assertTrue("Valid token", token!=0);
    }

}
