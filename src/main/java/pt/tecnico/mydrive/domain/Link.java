package pt.tecnico.mydrive.domain;

import java.util.ArrayList;

import org.jdom2.Element;
import org.joda.time.DateTime;

import pt.tecnico.mydrive.exception.*;

import pt.tecnico.mydrive.utils.*;

public class Link extends Link_Base {

    public Link(FileSystem fs, Directory parent, String name,
                User owner,    String content)
        throws AccessDeniedException, InvalidFileNameException,
               InvalidLinkContentException, AbsolutePathSizeLimitExceeded
    {
        if (!isValidContent(parent, content))
            throw new InvalidLinkContentException(content);
        init(fs, parent, name, owner, content);
    }

    public Link(FileSystem fs, String absolutePath)
        throws InvalidFileNameException, AccessDeniedException, AbsolutePathSizeLimitExceeded
    {
        init(fs, absolutePath);
    }

    public Link(FileSystem fs, Element element)
        throws ImportDocumentException, AccessDeniedException,
               InvalidFileNameException, AbsolutePathSizeLimitExceeded
    {
        init(fs, element);

        final String content =
            Utils.elementDefaultValue(element, "value", "");
        final String parentPath =
            Utils.elementDefaultValue(element, "path", "/home/root");

        final User root = fs.getRootUser();
        final Directory parent =
            (Directory) fs.getRootDirectory().getFile(parentPath, root);

        if (!isValidContent(parent, content))
            throw new InvalidLinkContentException(content);
        super.write(content, root);
    }

    private boolean isValidContent(Directory parent, String content) {
        FileSystem fs = FileSystem.getInstance();

        if (content.charAt(0) == '/')
            return fs.getRootDirectory().hasFile(content, fs.getRootUser());
        return parent.hasFile(content, fs.getRootUser());
    }

    @Override
    public String read(User u) throws AccessDeniedException {
        if (!u.hasReadPermission(this))
            throw new AccessDeniedException();

        return getPointedFile(u).read(u);
    }

    @Override
    public void write(String content, User u) throws AccessDeniedException {
        if (!u.hasWritePermission(this))
            throw new AccessDeniedException();

        getPointedFile(u).write(content, u);
    }

    @Override
    public void execute(User user, String[] args) throws AccessDeniedException {
        if (!user.hasExecutePermission(this))
            throw new AccessDeniedException();

        getPointedFile(user).execute(user, args);
    }

    public String getPointedPath() {
        final User root = FileSystem.getInstance().getRootUser();
        return super.read(root);
    }

    public File getPointedFile(User u) {
        final String path = super.read(u);
        if(path.charAt(0) == '/') {
            return FileSystem.getInstance().getRootDirectory().getFile(path, u);
        } else {
            return getParentDirectory().getFile(path, u);
        }
    }

    @Override
    public String toString() {
        return super.toString() + " -> " + super.getContent();
    }

    public Element xmlExport() {
        Element element = super.xmlExport();
        element.setName("link");
        element.getChild("contents").setName("value");
        return element;
    }


}
