package pt.tecnico.mydrive.presentation;

import java.util.TreeSet;

import pt.tecnico.mydrive.service.ListDirectoryService;
import pt.tecnico.mydrive.service.dto.EntryDto;

public class List extends MdCommand
{
    public List(MyDrive sh)
    {
        super(sh, "ls", "ls [path] : lists entries in the current directory " +
                        "(or in path, if given)");
    }

    @Override
    public void dispatch(String[] args)
    {
        final String path = args.length > 0 ? args[0] : ".";
        final MyDrive drive = (MyDrive) shell();

        final ListDirectoryService service =
            new ListDirectoryService(drive.getToken(), path);
        service.execute();

        for (EntryDto dto: new TreeSet<>(service.result())) {
            if (dto.fileName.equals("/")) {
                continue;
            }
            for (String s: entryDtoInfo(dto)) {
                drive.print(s);
                drive.print(" ");
            }
            if (dto.fileType.equals("LINK")) {
                drive.print(" -> " + dto.linkPointedPath);
            }
            drive.println("");
        }
    }

    private java.util.ArrayList<String> entryDtoInfo(EntryDto dto)
    {
        return new java.util.ArrayList<String>() {{
            add(dto.fileType);
            add(dto.permissions);
            add(Integer.toString(dto.size));
            add(dto.owner);
            add(Integer.toString(dto.id));
            add(dto.dateOfLastModification);
            add(dto.fileName);
        }};
    }

}
