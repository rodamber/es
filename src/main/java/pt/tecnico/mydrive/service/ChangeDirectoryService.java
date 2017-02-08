package pt.tecnico.mydrive.service;

import pt.tecnico.mydrive.domain.FileSystem;
import pt.tecnico.mydrive.domain.Login;
import pt.tecnico.mydrive.domain.Session;
import pt.tecnico.mydrive.domain.Directory;

import pt.tecnico.mydrive.exception.*;

public class ChangeDirectoryService extends MyDriveService {

    private long token;
    private String path;
    private String current;

    public ChangeDirectoryService(long token, String path) {
        this.token = token;
        this.path = path;
    }

    @Override
    public final void dispatch() throws SessionUnknownException, SessionExpiredException, EntryUnknownException,
                                         AccessDeniedException, IsNotDirectoryException {

    	Login l = getFileSystem().getLogin();

        l.sessionRenew(token);
        
        current = l.sessionChangeWorkingDirectory(token, path).getAbsolutePath();
    }
    
    public final String result() {
    	return current;
    }

}