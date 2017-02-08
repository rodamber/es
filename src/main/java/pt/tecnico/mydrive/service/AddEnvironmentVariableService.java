package pt.tecnico.mydrive.service;

import java.util.Set;
import static java.util.stream.Collectors.toSet;

import pt.tecnico.mydrive.domain.*;
import pt.tecnico.mydrive.exception.*;
import pt.tecnico.mydrive.service.dto.EnvVarDto;

import pt.ist.fenixframework.Atomic;

public class AddEnvironmentVariableService extends MyDriveService {
    final private long token;
    final private String name;
    final private String value;

    public AddEnvironmentVariableService(long token, String name, String value) {
        this.token = token;
        this.name = name;
        this.value = value;
    }

    @Override
    public final void dispatch()
            throws AccessDeniedException, SessionExpiredException,
                   SessionUnknownException
    {
        final Login login = getFileSystem().getLogin();
        login.sessionRenew(token);
        new EnvVar(login, this.name, this.value);
    }

    @Atomic
    public final Set<EnvVarDto> result() {
        return FileSystem.getInstance().getLogin().getEnvVarSet()
            .stream().map(ev -> new EnvVarDto(ev)).collect(toSet());
    }

}
