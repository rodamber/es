package pt.tecnico.mydrive.domain;

import pt.tecnico.mydrive.exception.*;

public class RootDirectory extends RootDirectory_Base {

    public RootDirectory(FileSystem fs) {
        init(fs, this, "b", fs.getRootUser());
        setName("/");
        fs.setRootDirectory(this);
    }

    @Override
    public void remove() throws IllegalRemovalException {
        throw new IllegalRemovalException(getName());
    }

    @Override
    public String getAbsolutePath() {
        return getAbsolutePathHelper();
    }

    @Override
    protected String getAbsolutePathHelper() {
        return getName();
    }

    @Override
    public File getFile(String path, User user) throws AccessDeniedException, EntryUnknownException {
        User root = FileSystem.getInstance().getRootUser();

        if (path.equals(getName())) {
            if (getFileSet(root).contains(this)) // Can't use hasFile because it has mutual recursivity with getFile.
                return this;
            throw new EntryUnknownException("Starting application, root directory does not exist yet");
        }

        return super.getFile(path, user);
    }

    @Override
    public String toString() {
        return toString("Directory", getName());
    }

}
