package pt.tecnico.mydrive.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import pt.tecnico.mydrive.exception.*;

import pt.tecnico.mydrive.domain.Login;
import pt.tecnico.mydrive.domain.File;
import pt.tecnico.mydrive.domain.PlainFile;
import pt.tecnico.mydrive.domain.User;
import pt.tecnico.mydrive.domain.Directory;

public class ReadFileService extends MyDriveService {
	
    private long token;
    private String fileName;
    private String contents;
        
    public ReadFileService(long token, String fileName) {
    	this.token = token;
        this.fileName = fileName;
    }    
    
    public final void dispatch() throws IsNotPlainFileException, SessionExpiredException,
                                        SessionUnknownException, EntryUnknownException,
                                        AccessDeniedException{

        Login l = getFileSystem().getLogin();
        
        l.sessionRenew(token);
        User u = l.sessionGetUser(token);
    	Directory workingDirectory = l.sessionGetWorkingDirectory(token);
        File file = workingDirectory.getFile(fileName, u);
        
        contents = file.read(u);
        
    }

    public final String result() {
        return contents;
    }

}
