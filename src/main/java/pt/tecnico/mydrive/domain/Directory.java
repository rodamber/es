package pt.tecnico.mydrive.domain;

import org.jdom2.Element;

import java.util.*;

import pt.tecnico.mydrive.exception.*;
import pt.tecnico.mydrive.utils.*;

public class Directory extends Directory_Base {

    public Directory() { super(); }

    public Directory(FileSystem fs, Directory parent, String name, User owner)
        throws AccessDeniedException, InvalidFileNameException, AbsolutePathSizeLimitExceeded {
        init(fs, parent, name, owner);
    }

    public Directory(FileSystem fs, String absolutePath)
        throws AccessDeniedException, InvalidFileNameException, AbsolutePathSizeLimitExceeded {
        init(fs, absolutePath);
    }

    public Directory(FileSystem fs, Element element)
        throws ImportDocumentException, AccessDeniedException,
               InvalidFileNameException, AbsolutePathSizeLimitExceeded {
        init(fs, element);
    }

    public int getSize() {
        User root = FileSystem.getInstance().getRootUser();
        return getFileSet(root).size() + 2; // All files + itself + parent directory.
    }

    public boolean isEmpty(){
        User root = FileSystem.getInstance().getRootUser();
        return getFileSet(root).isEmpty();
    }

    @Override
    public Set<File> getFileSet() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<File> getFileSet(User user) throws AccessDeniedException {
        if (user.hasExecutePermission(this))
            return super.getFileSet();
        throw new AccessDeniedException();
    }

    @Override
    public File getFile(String path, User user) throws AccessDeniedException, EntryUnknownException {
        if (path.equals("."))
            return this;
        if (path.equals(".."))
            return getParentDirectory();

        final String filename = path.substring(path.lastIndexOf("/") + 1);

        // Separates the filename from the rest of the path.
        String restOfThePath = "";
        if (path.lastIndexOf("/") >= 0)
            restOfThePath = path.substring(0, path.lastIndexOf("/"));

        if (!(restOfThePath.isEmpty())) // Not sure if this condition is necessary or not.
            return getFile(restOfThePath, user).getFile(filename, user);

        // Else path is just the filename.
        for (File file: getFileSet(user))
            if(file.getName().equals(filename))
                return file;
        throw new EntryUnknownException(filename);
    }

    public boolean hasFile(String name, User user) throws AccessDeniedException {
        try {
            getFile(name, user);
        } catch (EntryUnknownException e) {
            return false;
        }
        return true;
    }

    @Override
    public void addFile(File file) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addFile(File file, User user) throws AccessDeniedException, EntryExistsException {
        if (hasFile(file.getName(), user))
            throw new EntryExistsException(file.getName());
        if (user.hasWritePermission(file))
            super.addFile(file);
        else
            throw new AccessDeniedException();
    }

    @Override
    protected void remove(User user) throws AccessDeniedException, IllegalRemovalException {
        if (!user.hasDeletePermission(this))
            throw new AccessDeniedException();
        if (!isEmpty() || user.getHomeDirectory() == this)
            throw new IllegalRemovalException(getName());
        super.remove(user);
    }

    @Override
    protected void removeRecursively(User user) throws AccessDeniedException, IllegalRemovalException {
        for (File f: getFileSet(user))
            f.removeRecursively(user);
        remove(user);
    }

    @Override
    public void removeFile(String path, User user) throws EntryUnknownException, IllegalRemovalException, AccessDeniedException {
        if (path.equals(".") || path.equals(".."))
            throw new IllegalRemovalException(path);
        getFile(path, user).remove(user);
    }

    @Override
    public void removeFileRecursively(String path, User user) throws AccessDeniedException, EntryUnknownException,
                                                                     IllegalRemovalException {
        if (path.equals(".") || path.equals(".."))
            throw new IllegalRemovalException(path);
        getFile(path, user).removeRecursively(user);
    }

    public Element xmlExport() {
        Element element = super.xmlExport();
        element.setName("directory");
        return element;
    }

    @Override
    TreeSet<File> traverse(User user) /* throws AccessDeniedException */ {
        TreeSet<File> files = new TreeSet();
        files.add(this);

        for (File f: getFileSet(user))
            if (f != getParentDirectory())
                files.addAll(f.traverse(user));
        return files;
    }

}
