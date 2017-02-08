package pt.tecnico.mydrive.domain;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import pt.ist.fenixframework.FenixFramework;
import pt.tecnico.mydrive.exception.*;
import pt.tecnico.mydrive.utils.*;

public class FileSystem extends FileSystem_Base {
    static final Logger log = LogManager.getRootLogger();

    public static FileSystem getInstance() {
        FileSystem fileSystem = FenixFramework.getDomainRoot().getFileSystem();

        if (fileSystem != null)
            return fileSystem;

        log.trace("new FileSystem");
        return new FileSystem();
    }

    public FileSystem() {
        setRoot(FenixFramework.getDomainRoot());
        init();
    }

    protected void init() {
        setMaxFileId(0);
        setLogin(new Login());

        User rootUser = new RootUser(this);
        Directory rootDirectory = new RootDirectory(this);
        Directory homeDirectory = new Directory(this, getRootDirectory(), "home", null);

        rootUser.setHomeDirectory(new Directory(this, homeDirectory, "root", rootUser));

        new GuestUser(this);
    }

    /* package */ int incrementFileID() {
        int id = getMaxFileId();
        setMaxFileId(id + 1);
        return id;
    }

    public User getUser(String username) throws UserUnknownException {
        for(User user: getUserSet())
            if(user.getUsername().equals(username))
                return user;
        throw new UserUnknownException(username);
    }

    public boolean hasUser(String username) {
        try {
            getUser(username);
        } catch (UserUnknownException e) {
            return false;
        }
        return true;
    }

    @Override
    public void addUser(User user) throws UserExistsException {
        if (hasUser(user.getUsername()))
            throw new UserExistsException(user.getUsername());
        super.addUser(user);
    }

    @Override
    public void setRootUser(RootUser root) {
        if (root == null && getRootUser() != null)
            getRootUser().remove();
        super.setRootUser(root);
    }

    public long login(String username, String password) throws WrongPasswordException,
                                      SessionUnknownException, SessionExpiredException {
        return getLogin().login(this, getUser(username), password);
    }

    public void xmlImport(Document document) {
        log.trace("FileSystem.xmlImport(Document)");

        Element re = document.getRootElement();

        re.getChildren("user" ).forEach(c -> new User     (this, c));
        re.getChildren("dir"  ).forEach(c -> new Directory(this, c));
        re.getChildren("plain").forEach(c -> new PlainFile(this, c));
        re.getChildren("app"  ).forEach(c -> new App      (this, c));
        re.getChildren("link" ).forEach(c -> new Link     (this, c));
    }

    public Document xmlExport() {
        Element  element = new Element("mydrive");
        Document doc     = new Document(element);

        for (User user: getUserSet())
            element.addContent(user.xmlExport());

        TreeSet<File> files = getRootDirectory().traverse(getRootUser());
        for (File f: files)
            element.addContent(f.xmlExport());

        return doc;
    }

}
