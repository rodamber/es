package pt.tecnico.mydrive.service;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.*;

import org.joda.time.DateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import pt.tecnico.mydrive.domain.*;
import pt.tecnico.mydrive.exception.SessionUnknownException;
import pt.tecnico.mydrive.exception.SessionExpiredException;
import pt.tecnico.mydrive.service.LoginUserService;
import pt.tecnico.mydrive.service.LogoutUserService;
import pt.tecnico.mydrive.service.dto.EnvVarDto;

public class AddEnvironmentVariableTest extends AbstractServiceTest
{
    @Override
    protected void populate() { /* Empty */ }

    /* Tests */

    @Test(expected = SessionUnknownException.class)
    public void shouldFailToAddNewEnvVarWithUnknownSession()
    {
        final LoginUserService login = new LoginUserService("root", "***");
        login.execute();
        final long token = login.result();

        final LogoutUserService logout = new LogoutUserService(token);
        logout.execute();

        new AddEnvironmentVariableService(token, "name", "value").execute();
    }

    @Test(expected = SessionExpiredException.class)
    public void shouldFailToAddNewEnvVarWithExpiredSession()
    {
        final LoginUserService login = new LoginUserService("root", "***");
        login.execute();
        final long token = login.result();

        final Session session =
            FileSystem.getInstance().getLogin().getSession(token);
        session.setExpiryDate(new DateTime().minusHours(3));

        new AddEnvironmentVariableService(token, "name", "value").execute();
    }

    @Test
    public void shouldSucceedToAddNewEnvVar()
    {
        final LoginUserService login = new LoginUserService("root", "***");
        login.execute();
        final long token = login.result();

        final AddEnvironmentVariableService addVar =
            new AddEnvironmentVariableService(token, "name", "value");
        addVar.execute();

        final Predicate<EnvVarDto> p = ev ->
            ev.getName().equals("name") && ev.getValue().equals("value");
        assertTrue(addVar.result().stream().anyMatch(p));
    }

    @Test
    public void shouldSucceedToRedefineEnvVar()
    {
        final LoginUserService login = new LoginUserService("root", "***");
        login.execute();
        final long token = login.result();

        final String name  = "name";
        final String[] values = {"0", "1", "2", "3", "4"};

        for (final String value: values) {
            final AddEnvironmentVariableService addVar =
                new AddEnvironmentVariableService(token, name, value);
            addVar.execute();

            final Predicate<EnvVarDto> p = ev -> ev.getName().equals(name);
            final Set<EnvVarDto> vars = addVar.result();
            assertEquals(1, vars.stream().filter(p).count());

            final EnvVarDto var = vars.stream().filter(p).findFirst().get();
            assertTrue(var.getValue().equals(value));
        }
    }

}
