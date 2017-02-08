package pt.tecnico.mydrive.service;


import java.util.Arrays;

import pt.tecnico.mydrive.domain.*;
import pt.tecnico.mydrive.exception.*;

public class WriteFileService extends MyDriveService {

    private long token;
    private String path;
    private String content;

    public WriteFileService(long token, String path, String content) {
        this.token = token;
        this.path = path;
        this.content = content;
    }

    @Override
    public final void dispatch() throws SessionExpiredException, SessionUnknownException,
                                        EntryUnknownException, IsNotPlainFileException {
        FileSystem fs = getFileSystem();
        Session session = fs.getLogin().getSession(token);
        session.renew();
        
        Directory cwd = session.getCwd();
        User user = session.getUser();
        File file = null;
         
        if (path.charAt(0) == '/') { // Then path is an absolute path.
            file = fs.getRootDirectory().getFile(path, user);
        } else {
            file = cwd.getFile(path, user);
        }
        
        file.write(content, user);
    }

}
