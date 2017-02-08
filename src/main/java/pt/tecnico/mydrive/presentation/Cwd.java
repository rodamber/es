package pt.tecnico.mydrive.presentation;

import pt.tecnico.mydrive.service.ChangeDirectoryService;

public class Cwd extends MdCommand
{
    public Cwd(MyDrive sh)
    {
        super(sh, "cwd", "cwd [path] : changes current working directory to specified path and prints it");
    }

    @Override
    public void dispatch(String[] args)
    {
        final String path = args.length > 0 ? args[0] : ".";
        final MyDrive drive = (MyDrive) shell();

        final ChangeDirectoryService service =
            new ChangeDirectoryService(drive.getToken(), path);
        service.execute();

        drive.println(service.result());
    }

}
