package pt.tecnico.mydrive.presentation;

import pt.tecnico.mydrive.service.ExecuteFileService;

import java.util.Arrays;

public class Execute extends MdCommand {

    public Execute(MyDrive md) {
        super(md, "do", "do path [args] : executes a plain file on the given path with optional arguments");
    }

    @Override
    public void dispatch(String[] args) throws RuntimeException {
	if (args.length == 0)
	    throw new RuntimeException("USAGE: " + name() + " <path> [<args>]");

    String[] arguments = null;

    if (args.length > 1)
        arguments = Arrays.copyOfRange(args, 1, args.length);

    final MyDrive md = (MyDrive) shell();

    new ExecuteFileService(md.getToken(), args[0], arguments).execute();
    }

}
