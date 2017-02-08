package pt.tecnico.mydrive.domain;

import java.util.ArrayList;
import java.util.Arrays;

import org.jdom2.Element;
import org.joda.time.DateTime;

import pt.tecnico.mydrive.exception.*;

import pt.tecnico.mydrive.utils.*;

public class PlainFile extends PlainFile_Base {

    public PlainFile() {}

    public PlainFile(FileSystem fs, Directory parent, String name, User owner)
                                            throws AccessDeniedException, InvalidFileNameException,
                                            AbsolutePathSizeLimitExceeded {
        this(fs, parent, name, owner, "");
    }

    public PlainFile(FileSystem fs, Directory parent, String name, User owner, String content)
                                            throws AccessDeniedException, InvalidFileNameException,
                                            AbsolutePathSizeLimitExceeded {

        init(fs, parent, name, owner, content);
    }

    public PlainFile(FileSystem fs, String absolutePath) throws AccessDeniedException,
                                                                InvalidFileNameException,
                                                                AbsolutePathSizeLimitExceeded {
        init(fs, absolutePath);
    }

    public PlainFile(FileSystem fs, Element element) throws ImportDocumentException,
                                     AccessDeniedException, InvalidFileNameException,
                                     AbsolutePathSizeLimitExceeded {
        init(fs, element);
        super.setContent(Utils.elementDefaultValue(element, "contents", ""));
    }

    @Override
    protected void init (FileSystem fs, Directory parent, String name, User owner)
                                        throws AccessDeniedException, InvalidFileNameException,
                                        AbsolutePathSizeLimitExceeded {
        init(fs, parent, name, owner, "");
    }

    protected void init (FileSystem fs, Directory parent, String name, User owner, String content)
                                        throws AccessDeniedException, InvalidFileNameException,
                                        AbsolutePathSizeLimitExceeded {
        super.init(fs, parent, name, owner);
        super.setContent(content);
    }

    @Override
    public int getSize() {
        return super.getContent().length();
    }

    @Override
    public String getContent() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setContent(String content) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String read(User user) throws AccessDeniedException {
        if (!user.hasReadPermission(this))
            throw new AccessDeniedException();
        return super.getContent();
    }

    @Override
    public void write(String content, User user) throws AccessDeniedException {
        if (!user.hasWritePermission(this))
            throw new AccessDeniedException();
        super.setContent(content);
    }
    
    @Override
    public void execute(User user, String[] args) 
            throws AccessDeniedException, IllegalArgumentException {
        // PlainFile doesn't need arguments, because has its own on content.
        if (!user.hasExecutePermission(this))
            throw new AccessDeniedException();
        
        String content = read(user);
        if(content.isEmpty()){
            throw new IllegalArgumentException("Plain file " + getName() + " is empty");
        }
        
        String[] lines = content.split("\n");
        for (String line : lines) {
            executeLine(user, line);
        }
    }
    
    public void executeLine(User user, String line) throws IsNotAppException {
        
        String lineSplitted[] = line.split(" ", 2);
        
        // Getting app
        String appPath = lineSplitted[0];
        
        File file = null;
        if(appPath.charAt(0) == '/')
            file = FileSystem.getInstance().getRootDirectory().getFile(appPath, user);
        else
            file = getParentDirectory().getFile(appPath, user);
        
        if (!(file instanceof App)){
            throw new IsNotAppException(file.getName());
        }
        
        // Getting arguments
        String appArgs = "";
        String args[] = new String[0];
        
        if(lineSplitted.length == 2){
            appArgs = lineSplitted[1];
            args = appArgs.split(" ");  
        }
        
        ((App)file).execute(user, args);
    }

    public Element xmlExport() {
        Element element = super.xmlExport();
        element.setName("plainFile");
        element.addContent(new Element("contents").addContent(super.getContent()));
        return element;
    }

}
