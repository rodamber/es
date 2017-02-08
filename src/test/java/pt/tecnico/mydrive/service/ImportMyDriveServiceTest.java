package pt.tecnico.mydrive.service;

import pt.tecnico.mydrive.domain.*;
import pt.tecnico.mydrive.service.*;
import pt.tecnico.mydrive.exception.*;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.io.FileNotFoundException;

import java.util.*;

import static org.junit.Assert.*;
import org.junit.Test;

public class ImportMyDriveServiceTest extends AbstractServiceTest {
    @Override
    protected void populate() { /* Empty */ }

    public void xmlScan(final java.io.File file) throws IOException {
        final SAXBuilder builder = new SAXBuilder();
        try {
            final Document doc = (Document) builder.build(file);
            new ImportMyDriveService(doc).execute();
        } catch (JDOMException e) {
            e.printStackTrace();
        }
    }

    /* Tests */

    @Test
    public void xmlImportBroadCoverageFileTest() throws IOException {
        xmlScan(new java.io.File("res/drive.xml"));

        final FileSystem fs = FileSystem.getInstance();
        final User rootUser = fs.getRootUser();
        final Directory rootDir = fs.getRootDirectory();

        assertEquals(4, fs.getUserSet().size()); // root + guest + jtb + mja

        final User jtb = fs.getUserSet().stream().filter(u -> u.getUsername().equals("jtb")).findFirst().get();
        assertEquals("Joaquim Teófilo Braga", jtb.getName());

        final File homeJtb = rootDir.getFile("/home/jtb", rootUser);
        assertEquals(jtb.getUsername(), homeJtb.getOwner().getUsername());

        final File profile = rootDir.getFile("/home/jtb/profile", rootUser);
        assertEquals("profile", profile.getName());

        final Link doc = (Link) rootDir.getFile("/home/jtb/doc", rootUser);
        assertEquals("/home/jtb/documents", doc.getPointedFile(rootUser).getAbsolutePath());

        final File hello = rootDir.getFile("/home/jtb/bin/hello", rootUser);
        assertEquals("rwxd--x-", hello.getPermissions().toString());
    }

    @Test(expected = FileNotFoundException.class)
    public void xmlImportNonExistentFileTest() throws IOException {
        xmlScan(new java.io.File("res/file-that-does-not-exist-for-sure.123"));
    }

    @Test(expected = InvalidPasswordException.class)
    public void xmlImportShortUsernameTest() throws IOException {
        xmlScan(new java.io.File("res/baddrive.xml"));
    }

}
