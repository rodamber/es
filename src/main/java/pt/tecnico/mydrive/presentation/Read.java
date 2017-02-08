package pt.tecnico.mydrive.presentation;

import pt.tecnico.mydrive.service.ReadFileService;

public class Read extends MdCommand
{
    public Read(MyDrive drive)
    {
        super(drive, "read", "read fileName : prints file content" );
    }

    @Override
    public void dispatch(String[] args)
    {
        final MyDrive drive = (MyDrive) shell();

        if (args.length != 1)
            throw new RuntimeException("USAGE: "+name()+" <fileName>");

        final ReadFileService service = new ReadFileService(drive.getToken(), args[0]);
        service.execute();
        drive.println(service.result());

    }

}
