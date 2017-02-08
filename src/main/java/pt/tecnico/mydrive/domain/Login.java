package pt.tecnico.mydrive.domain;

import java.util.Random;
import java.math.BigInteger;

import pt.tecnico.mydrive.exception.*;

public class Login extends Login_Base {

    public Login() {
        super();
    }

    public long login(FileSystem fs, User user, String password) throws UserUnknownException, WrongPasswordException,
                                                                        SessionUnknownException, SessionExpiredException {
        if( !fs.hasUser(user.getUsername()) )
            throw new UserUnknownException( user.getUsername() );
        if ( !user.checkPassword(password) )
            throw new WrongPasswordException( user.getUsername() );

        final long token = generateToken();
        new Session(this, user, token);

        removeInactiveSessions();
        return token;
    }

    public EnvVar getEnvVar(String name) throws EnvVarUnknownException {
        for (EnvVar var: getEnvVarSet())
            if (var.getName().equals(name))
                return var;
        throw new EnvVarUnknownException(name);
    }

    public boolean hasEnvVar(String name) {
        try {
            getEnvVar(name);
        } catch (EnvVarUnknownException e) {
            return false;
        }
        return true;
    }

    @Override
    public void addEnvVar(EnvVar var) {
        if (hasEnvVar(var.getName())) {
            // If var already exists, redefine it.
            getEnvVar(var.getName()).setValue(var.getValue());
        } else {
            super.addEnvVar(var);
        }
    }

    private void removeInactiveSessions() {
        for(Session s: getSessionSet()) {
            if (!s.isValid())
                s.remove();
        }
    }

    public Session getSession(long token) throws SessionUnknownException, SessionExpiredException {
        for(Session s: getSessionSet()) {

            if(s.tokenEquals(token)) {
                if (s.isValid())
                    return s;
                else {
                    s.remove();
                    throw new SessionExpiredException();
                }
            }
        }
        throw new SessionUnknownException();
    }

    public void removeSession(long token) throws SessionExpiredException, SessionUnknownException {
        getSession(token).remove();
    }

    public boolean sessionIsValid(long token) throws SessionUnknownException,
                                                     SessionExpiredException {
        Session s = getSession(token);
        return s.isValid();
    }

    public User sessionGetUser(long token) throws SessionUnknownException,
                                                  SessionExpiredException {
        Session s = getSession(token);
        return s.getUser();
    }

    public Directory sessionChangeWorkingDirectory(long token, String path)
                                        throws SessionUnknownException, SessionExpiredException,
                                               AccessDeniedException,   IsNotDirectoryException {
        Session s = getSession(token);

        return s.changeWorkingDirectory(path);
    }

    public Directory sessionGetWorkingDirectory(long token)
                                        throws SessionUnknownException, SessionExpiredException {
        Session s = getSession(token);
        return s.getCwd();
    }

    public void sessionRenew(long token) throws SessionUnknownException, SessionExpiredException {
        Session s = getSession(token);
        s.renew();
    }

    public boolean sessionHasLoggedUser(long token, String username)
                                        throws SessionUnknownException, SessionExpiredException {
        Session s = getSession(token);
        return s.hasLoggedUser(username);
    }

    private long generateToken() {
        long token = new BigInteger(64, new Random()).longValue();
        if (token < 0) token = token*-1;

        while ( tokenExists(token) ) {
            token = new BigInteger(64, new Random()).longValue();
            if (token < 0) token = token*-1;
        }

        return token;
    }

    private boolean tokenExists(long token) {
        for(Session s: getSessionSet()) {
            if(s.tokenEquals(token))
                return true;
        }
        return false;
    }

}
