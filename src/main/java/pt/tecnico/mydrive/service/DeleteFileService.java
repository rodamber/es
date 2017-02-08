package pt.tecnico.mydrive.service;

import pt.tecnico.mydrive.domain.*;
import pt.tecnico.mydrive.exception.*;

public class DeleteFileService extends MyDriveService {

    private long token;
    private String fileName;

    public DeleteFileService(long token, String fileName) {
        this.token = token;
        this.fileName = fileName;
    }

    @Override
    public final void dispatch() throws SessionUnknownException, SessionExpiredException,
                                        EntryUnknownException, IllegalRemovalException,
                                        AccessDeniedException {
        final FileSystem fs = FileSystem.getInstance();
        final Session session = fs.getLogin().getSession(token);

        session.renew();
        session.getCwd().removeFileRecursively(fileName, session.getUser());
    }

}
