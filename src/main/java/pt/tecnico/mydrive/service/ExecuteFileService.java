package pt.tecnico.mydrive.service;

import pt.tecnico.mydrive.domain.*;
import pt.tecnico.mydrive.exception.*;

import java.util.ArrayList;

public class ExecuteFileService extends MyDriveService {
    private long token;
    private String filePath;
    private String[] args;

    public ExecuteFileService(long token, String path, String[] args) {
        this.token = token;
        this.filePath = path;
        this.args = args;
    }

    @Override
    public final void dispatch()
            throws AccessDeniedException, IsNotDirectoryException, EntryUnknownException {
        FileSystem fs = getFileSystem();
        Login login = fs.getLogin();
        login.sessionRenew(token);

        Session session = login.getSession(token);
        User user = session.getUser();
        Directory cwd = session.getCwd();

        File file = null;
        if(filePath.charAt(0) == '/')
            file = FileSystem.getInstance().getRootDirectory().getFile(filePath, user);
        else
            file = cwd.getFile(filePath, user);

        file.execute(user, args);

    }

}
