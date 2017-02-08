package pt.tecnico.mydrive.service.dto;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import pt.tecnico.mydrive.domain.*;
import pt.tecnico.mydrive.exception.*;

public class EntryDto implements Comparable<EntryDto>
{
    public static String APP       = "App";
    public static String DIRECTORY = "Directory";
    public static String LINK      = "Link";
    public static String PLAINFILE = "PlainFile";

    public String fileType                = null;
    public String fileName                = null;
    public String parentName              = null;
    public String permissions             = null;
    public String owner                   = null;
    public int    size                    = -1;
    public int    id                      = -1;
    public String dateOfLastModification  = null;
    public String content                 = null;
    public String linkPointedPath         = null;

    // Factory method.
    // RODRIGO:FIXME: Ask the professor if there is a better way to this...
    public static EntryDto create(File file, User user)
        throws IllegalArgumentException
    {
        if (file == null) {
            throw new IllegalArgumentException("Null file.");
        }

        EntryDto dto = new EntryDto();

        dto.fileName    = file.getName();
        dto.permissions = file.getPermissions().toString();
        dto.parentName  = file.getParentDirectory().getName();
        dto.owner       = file.getOwner().getUsername();
        dto.size        = file.getSize();
        dto.id          = file.getId();
        dto.dateOfLastModification =
            DateTimeFormat.forPattern("YYYY MMM d HH:mm").print(file.getLastModified());

        if (file instanceof App) {
            dto.fileType = EntryDto.APP;
        } else if (file instanceof Directory) {
            dto.fileType = EntryDto.DIRECTORY;
        } else if (file instanceof Link) {
            dto.fileType = EntryDto.LINK;
            Link link = (Link) file;
            dto.linkPointedPath = link.getPointedPath();
        } else if (file instanceof PlainFile) {
            dto.fileType = EntryDto.PLAINFILE;
        } else {
            throw new IllegalArgumentException("Illegal file type.");
        }

        try {
            dto.content = file.read(user);
        } catch (AccessDeniedException | IsNotPlainFileException | UnsupportedOperationException e) {
            // No problem with that; just ignore it.
            // Upper layers will have to check if content is null or not.
        }
        return dto;
    }

    @Override
    public int compareTo(EntryDto other) { return id - other.id; }
}
