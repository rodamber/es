package pt.tecnico.mydrive.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.ist.fenixframework.Atomic;

import pt.tecnico.mydrive.domain.FileSystem;

import pt.tecnico.mydrive.exception.MyDriveException;

public abstract class MyDriveService {
    protected static final Logger log = LogManager.getRootLogger();

    @Atomic
    public final void execute() throws MyDriveException {
        dispatch();
    }

    static FileSystem getFileSystem() {
        return FileSystem.getInstance();
    }

    protected abstract void dispatch() throws MyDriveException;
}
