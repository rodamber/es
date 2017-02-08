package pt.tecnico.mydrive.service;

import java.util.Set;
import java.util.TreeSet;

import pt.tecnico.mydrive.domain.*;
import pt.tecnico.mydrive.exception.*;
import pt.tecnico.mydrive.service.dto.*;

public class ListDirectoryService extends MyDriveService {

    private long token;
    private Set<EntryDto> entries;
    private String path = null;

    public ListDirectoryService(long token, String path) {
        this.token = token;
        this.entries = new TreeSet<EntryDto>();
        this.path = path;
    }

    @Override
    public final void dispatch()
        throws SessionUnknownException, SessionExpiredException,
               AccessDeniedException, IsNotDirectoryException
    {
        final FileSystem fs = getFileSystem();
        final Login login = fs.getLogin();

        login.sessionRenew(token);

        final User user = login.getSession(token).getUser();
        final File cwd = login.sessionGetWorkingDirectory(token);

        File dirToList;
        if (path.charAt(0) == '/') { // Then path is an absolute path.
            dirToList = fs.getRootDirectory().getFile(path, user);
        } else {
            dirToList = path == null ? cwd : cwd.getFile(path, user);
        }

        if (!(dirToList instanceof Directory) && !(dirToList instanceof Link)) {
            throw new IsNotDirectoryException(dirToList.getName());
        }

        if (dirToList instanceof Link) {
            dirToList = ((Link) dirToList).getPointedFile(user);
        }

        final Set<File> filesToList = dirToList.getFileSet(user);

        for (File f : filesToList) {
            this.entries.add(EntryDto.create(f, user));
        }
    }

    public final Set<EntryDto> result(){
        return entries;
    }
}
