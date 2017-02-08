package pt.tecnico.mydrive.presentation;

import pt.tecnico.mydrive.service.CreateFileService;

public class Create extends MdCommand
{
    public Create(MyDrive drive)
    {
        super(drive, "create", "create fileName fileType [content]: creates a new file of type (fileType) with name (fileName) and content optional. Directory, PlainFile, App, Link are possible fileTypes" );
    }

    @Override
    public void dispatch(String[] args)
    {
        final MyDrive drive = (MyDrive) shell();

        if(args.length == 2)
            new CreateFileService(drive.getToken(), args[0], args[1]).execute();
        else if(args.length == 3)
            new CreateFileService(drive.getToken(), args[0], args[1], args[2]).execute();
        else
            throw new RuntimeException("USAGE: "+name()+" <fileName> <fileType> [<content>]");

    }

}
