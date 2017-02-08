package pt.tecnico.mydrive.presentation;

import pt.tecnico.mydrive.service.WriteFileService;

public class Write extends MdCommand
{
    public Write(MyDrive drive)
    {
        super(drive, "update", "update path text: overwrites file content (file given by path)" );
    }

    @Override
    public void dispatch(String[] args)
    {
        final MyDrive drive = (MyDrive) shell();

        if (args.length != 2)
            throw new RuntimeException("USAGE: "+name()+" <path> <text>");

        new WriteFileService(drive.getToken(), args[0], args[1]).execute();
    }

}
