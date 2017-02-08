package pt.tecnico.mydrive.domain;

import org.joda.time.DateTime;

import pt.tecnico.mydrive.exception.*;

public class Session extends Session_Base {

    public Session(Login login, User user, long token) {
    	user.verifySessionPassword();
    	
        setLogin(login);
        setCwd(user.getHomeDirectory());
        setUser(user);
        setToken(token, user);
        renew();
    }

    @Override
    public void setLogin(Login login) {
        if (login == null)
            super.setLogin(login);
        else {
            login.addSession(this);
        }
    }

    public void remove() {
        setLogin(null);
        setCwd(null);
        setUser(null);
        deleteDomainObject();
    }

    public void renew(){
        DateTime now = new DateTime();
        setExpiryDate(now.plus(getUser().getSessionDuration()));
    }

    public boolean isValid() {
        DateTime now = new DateTime();
        return getExpiryDate().isAfter(now) || getUser() instanceof GuestUser;
    }

    public boolean hasLoggedUser(String username){
        return username.equals(getUser().getUsername());
    }

    public boolean tokenEquals(long token){
        return super.getToken() == token;
    }

    @Override
    public long getToken() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setToken(long token) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public void setToken(long token, User user) throws AccessDeniedException {
        if (user.getUsername().equals(getUser().getUsername())) {
            super.setToken(token);
        } else {
            throw new AccessDeniedException();
        }
    }

    public Directory changeWorkingDirectory(String path)
            throws AccessDeniedException, IsNotDirectoryException, EntryUnknownException {
        String absPath;
        if(path.charAt(0) == '/')
            absPath = path;
        else
            absPath = getCwd().getAbsolutePath() + "/" + path;

        FileSystem fs = getLogin().getFileSystem();
        File file = fs.getRootDirectory().getFile(absPath, getUser());

        if(!(file instanceof Directory))
            throw new IsNotDirectoryException(file.getName());
        setCwd((Directory) file);

        return getCwd();
    }

}
