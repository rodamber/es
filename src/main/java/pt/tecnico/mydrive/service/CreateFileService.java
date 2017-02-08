package pt.tecnico.mydrive.service;

import pt.tecnico.mydrive.domain.FileSystem;
import pt.tecnico.mydrive.domain.Login;
import pt.tecnico.mydrive.domain.Session;
import pt.tecnico.mydrive.domain.Directory;
import pt.tecnico.mydrive.domain.PlainFile;
import pt.tecnico.mydrive.domain.App;
import pt.tecnico.mydrive.domain.Link;

import pt.tecnico.mydrive.exception.SessionUnknownException;
import pt.tecnico.mydrive.exception.SessionExpiredException;
import pt.tecnico.mydrive.exception.EntryExistsException;
import pt.tecnico.mydrive.exception.InvalidFileNameException;
import pt.tecnico.mydrive.exception.AccessDeniedException;
import pt.tecnico.mydrive.exception.AbsolutePathSizeLimitExceeded;


public class CreateFileService extends MyDriveService {

    private long token;
    private String fileName;
    private String fileType;
    private String content;

    public CreateFileService(long token, String fileName, String fileType) {
        this.token = token;
        this.fileName = fileName;
        this.fileType = fileType;
        this.content = "";
    }

    public CreateFileService(long token, String fileName, String fileType, String content) {
        this(token, fileName, fileType);
        this.content = content;
    }

    @Override
    public final void dispatch()
            throws SessionUnknownException, SessionExpiredException,
                      EntryExistsException, InvalidFileNameException,
                                            AccessDeniedException, AbsolutePathSizeLimitExceeded {

        FileSystem fs = getFileSystem();
        Login l = fs.getLogin();

        l.sessionRenew(token); //throws SessionUnknown and SessionExpired

        switch (fileType) {
            case "Directory": new Directory(fs, l.sessionGetWorkingDirectory(token), fileName, l.sessionGetUser(token));
                break;
            case "PlainFile": new PlainFile(fs, l.sessionGetWorkingDirectory(token), fileName, l.sessionGetUser(token), content);
                break;
            case "App": new App(fs, l.sessionGetWorkingDirectory(token), fileName, l.sessionGetUser(token), content);
                break;
            case "Link": new Link(fs, l.sessionGetWorkingDirectory(token), fileName, l.sessionGetUser(token), content);
                break;
            default: System.out.println(fileType + " is not a file type\n");
                break;
        }
    }

}
