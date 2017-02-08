package pt.tecnico.mydrive.service;

import pt.tecnico.mydrive.domain.FileSystem;
import pt.tecnico.mydrive.exception.*;

public class LogoutUserService extends MyDriveService {
    private long token;

    public LogoutUserService(long token) {
        this.token = token;
    }

    @Override
    public final void dispatch() throws SessionExpiredException, SessionUnknownException {
        MyDriveService.getFileSystem().getLogin().removeSession(this.token);
    }
}
