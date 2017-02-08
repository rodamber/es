package pt.tecnico.mydrive.presentation;

import pt.tecnico.mydrive.exception.MyDriveException;
import pt.tecnico.mydrive.exception.SessionExpiredException;

public abstract class MdCommand extends Command {
    public MdCommand(Shell sh, String n)           { super(sh, n);    }
    public MdCommand(Shell sh, String n, String h) { super(sh, n, h); }

    public boolean debug = true;

    /* package */ void execute(String[] args) {
        try {
            dispatch(args);
        } catch (SessionExpiredException e) {
            if (debug) {
                e.printStackTrace();
            } else {
                shell().println(e.getMessage());
            }
            MyDrive md = (MyDrive) shell();
            md.removeToken(md.getToken());
            md.setToken(-1); //invalid token

        } catch (MyDriveException e) {
            if (debug) {
                e.printStackTrace();
            } else {
                shell().println(e.getMessage());
            }
        }
    }

    abstract void dispatch(String[] args) throws MyDriveException;

}
