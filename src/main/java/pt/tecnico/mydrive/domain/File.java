package pt.tecnico.mydrive.domain;

import org.jdom2.Element;
import org.joda.time.DateTime;

import java.util.*;

import pt.tecnico.mydrive.exception.*;
import pt.tecnico.mydrive.utils.*;

public abstract class File extends File_Base implements Comparable<File> {
	private final int pathMaxLength = 1024;

    protected void init(final FileSystem fs, final Directory parent,
                        final String name,   User owner)
        throws AccessDeniedException, InvalidFileNameException, IllegalArgumentException, AbsolutePathSizeLimitExceeded
    {
        if (fs == null)
            throw new IllegalArgumentException("FileSystem (fs) is null");
        if (parent == null)
            throw new IllegalArgumentException("Directory (parent) is null");
        if (name == null)
            throw new IllegalArgumentException("String (name) is null");
        if (owner == null)
            owner = fs.getRootUser();
        if (!isValidName(name))
            throw new InvalidFileNameException(name);
        if (owner != null && !owner.hasWritePermission(parent))
            throw new AccessDeniedException();
        if ((parent.getAbsolutePath() + "/" + name).length() > pathMaxLength)
        	throw new AbsolutePathSizeLimitExceeded(pathMaxLength);

        // setName must come before setParentDirectory because the latter
        // depends on the former.
        setName(name);

        setParentDirectory(parent);
        setId(fs.incrementFileID());
        setLastModified(new DateTime());
        setOwner(owner);
        setPermissions(new Permissions(owner.getUmask()));
    }

    protected void init(FileSystem fs, String relativePath, Directory parent)
        throws AccessDeniedException, InvalidFileNameException,
               IllegalArgumentException
    {
        if (fs == null)
            throw new IllegalArgumentException("FileSystem (fs) is null");
        if (relativePath == null)
            throw new IllegalArgumentException("String (relativePath) is null");
        if (parent == null)
            throw new IllegalArgumentException("Directory (parent) is null");

        if ((parent.getAbsolutePath() + "/" + relativePath).length() > pathMaxLength)
        	throw new AbsolutePathSizeLimitExceeded(pathMaxLength);

        List<String> directoriesNames = Arrays.asList(relativePath.split("/"));
        String newFile = directoriesNames.get(0);

        if (directoriesNames.isEmpty())
            throw new InvalidFileNameException("File constructor: Empty path");
        if (directoriesNames.size() == 1) {
            init(fs, parent, newFile, fs.getRootUser());
            return;
        }

        if (!parent.hasFile(newFile, fs.getRootUser()))
            new Directory(fs, parent, newFile, null);

        String nextRelativePath = String.join("/", directoriesNames.subList(1, directoriesNames.size()));
        Directory nextParent    = (Directory) parent.getFile(newFile, fs.getRootUser());

        init(fs, nextRelativePath, nextParent);
    }

    protected void init(FileSystem fs, String absolutePath)
        throws AccessDeniedException, InvalidFileNameException,
               IllegalArgumentException
    {
        String relativePath = absolutePath.substring(1); // Remove leading "/".
        init(fs, relativePath, fs.getRootDirectory());
    }

    protected void init(FileSystem fs, Element element)
        throws ImportDocumentException, AccessDeniedException,
               InvalidFileNameException, IllegalArgumentException
    {
        // Ignoring the file id to avoid id clashes (they must be unique).

        final String name        = Utils.elementDefaultValue(element, "name", Long.toString(Utils.generateDateID()));
        final String owner       = Utils.elementDefaultValue(element, "owner", "root");
        final String permissions = Utils.elementDefaultValue(element, "perm", "rwxd----");
        final String path        = Utils.elementDefaultValue(element, "path", "/home/root");

        init(fs, path + "/" + name);

        setOwner(fs.getUser(owner));
        setPermissions(new Permissions(permissions));
    }

    @Override
    public void setParentDirectory(Directory parent) throws EntryExistsException {
        if (parent != null)
            parent.addFile(this, FileSystem.getInstance().getRootUser());
        super.setParentDirectory(parent);
    }

    public String getAbsolutePath() {
        return getAbsolutePathHelper().replaceFirst(".$",""); // Remove last character ('/').
    }

    protected String getAbsolutePathHelper() {
        return getParentDirectory().getAbsolutePathHelper() + getName() + "/";
    }

    private boolean isValidName(String content) {
        return !content.matches("[/\0]");
    }

    protected void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    protected void remove(User user) throws AccessDeniedException {
        if (!user.hasDeletePermission(this))
            throw new AccessDeniedException();

        setParentDirectory(null);
        setOwner(null);
        setPermissions(null);
        deleteDomainObject();
    }

    protected void removeRecursively(User user) throws AccessDeniedException {
        remove(user);
    }

    public Element xmlExport() {
        Element element = new Element("file");

        element.setAttribute("id", Integer.toString(getId()));
        element.addContent(new Element("path").addContent(getParentDirectory().getAbsolutePath()));
        element.addContent(new Element("name").addContent(getName()));
        element.addContent(new Element("lastModified").addContent(getLastModified().toString("yyyyMMddhhmmss")));
        element.addContent(new Element("owner").addContent(getOwner().getUsername()));

        if (getPermissions() != null) // RODRIGO:FIXME: Horrible hack... getPermissions() should *not* be null
            element.addContent(new Element("perm").addContent(getPermissions().toString()));

        return element;
    }

    @Override
    public int compareTo(File f) {
        return getId() - f.getId();
    }

    @Override
    public String toString() {
        return toString(getClass().getSimpleName(), getName());
    }

    protected String toString(String fileType, String name) {
        // may give null pointer beacause fenixFramework uses toString to
        // warn users of inconsistencies
        String username = "";
        if(getOwner() != null) username = getOwner().getUsername();
        else username = "-undefined-";

        return fileType                               + " " +
            getPermissions()                          + " " +
            getSize()                                 + " " +
            username                                  + " " +
            getId()                                   + " " +
            getLastModified().toString("yyyy-MMM-dd") + " " +
            name;

    }

    TreeSet<File> traverse(User user) throws AccessDeniedException {
        if (!(user.hasReadPermission(this)))
            throw new AccessDeniedException();
        TreeSet<File> files = new TreeSet();
        files.add(this);
        return files;
    }

    public File getFile(String path, User user) throws UnsupportedOperationException {
        // Horrible hack to enjoy Java runtime dynamic binding.
        throw new UnsupportedOperationException();
    }

    public Set<File> getFileSet(User user) throws UnsupportedOperationException {
        // Horrible hack to enjoy Java runtime dynamic binding.
        throw new UnsupportedOperationException();
    }

    public void addFile(File file, User user) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public void removeFile(String path, User user) throws UnsupportedOperationException {
        // Horrible hack to enjoy Java runtime dynamic binding.
        throw new UnsupportedOperationException();
    }

    public void removeFileRecursively(String path, User user) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public String read(User user) throws IsNotPlainFileException  {
        throw new IsNotPlainFileException(getName());
    }

    public void write(String content, User user) throws IsNotPlainFileException {
        throw new IsNotPlainFileException(getName());
    }

    public void execute(User user, String[] args) throws IsNotPlainFileException {
        // NotExecutableException means almost the same, but it's more understandable.
        throw new IsNotPlainFileException(getName());
    }

    public abstract int getSize();

}
