package pt.tecnico.mydrive.domain;

public class FileExtension extends FileExtension_Base {
    
    public FileExtension(User u) {
        super();
        setFileExtensionUserRelation(u);
    }
    
    public FileExtension(User u, String name) {
        super();
        setFileExtensionUserRelation(u);
        setName(name);
    }
    
    public FileExtension(User u, String name, App app) {
        super();
        setFileExtensionUserRelation(u);
        setName(name);
        setApp(app);
    }

    
    @Override
    public void setUser(User u) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
    
    private void setFileExtensionUserRelation(User u) {
        if (u != null)
            u.addFileExtension(this);
        super.setUser(u);
    }
    
    public void remove() {
        setUser(null);
        setApp(null);
        deleteDomainObject();
    }
    
}
