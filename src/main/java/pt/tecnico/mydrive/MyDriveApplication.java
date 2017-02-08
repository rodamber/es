package pt.tecnico.mydrive;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.File;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import org.joda.time.DateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

import pt.tecnico.mydrive.domain.*;
import pt.tecnico.mydrive.service.*;

public class MyDriveApplication {
    static final Logger log = LogManager.getRootLogger();

    public static void main(String[] args) throws IOException {
        System.out.println("*** Welcome to the MyDrive application! ***");
        try {
            for (String s: args) xmlScan(new File(s));
            run();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FenixFramework.shutdown();
        }
    }

    @Atomic
    public static void run() {
        log.trace("Run: " + FenixFramework.getDomainRoot());

        final FileSystem fs = FileSystem.getInstance();
        final RootUser rootUser = fs.getRootUser();

        new User(fs, "bernardofcordeiro");
        new User(fs, "bytefewder");
        new User(fs, "marianaribeiro");
        new User(fs, "rodamber");
        //new User(fs, "sirwatt");
        new User(fs, "sirwatt", "sirwatt", "sirwattpassword", null, null);
        new User(fs, "volxfort");

        log.trace("1. Create /home/README");
        String listOfUsers = "";
        for(User user: fs.getUserSet())
            listOfUsers += user.getUsername() + "\n";

        Directory home   = (Directory) fs.getRootDirectory().getFile("home", rootUser);
        PlainFile readme = new PlainFile(fs, home, "README", fs.getRootUser(), listOfUsers);

        log.trace("2. Create /usr/local/bin");
        new Directory(fs, "/usr/local/bin");

        log.trace("3. Print /home/README");
        System.out.println(readme.read(fs.getRootUser()));

        log.trace("4. Remove /usr/local/bin");
        fs.getRootDirectory().removeFile("usr/local/bin", rootUser);

        log.trace("5. XML export");
        xmlPrint();

        log.trace("6. Remove /home/README");
        home.removeFile("README", rootUser);

        // log.trace("7. List /home");
        // home.listEntries(rootUser);

    }

    @Atomic
    public static void xmlPrint() {
        log.trace("xmlPrint: " + FenixFramework.getDomainRoot());

        Document doc = FileSystem.getInstance().xmlExport();
        XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());

        try {
            xmlOutput.output(doc, new PrintStream(System.out));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Atomic
    public static void xmlScan(File file) {
        log.trace("xmlScan: " + FenixFramework.getDomainRoot());
        SAXBuilder builder = new SAXBuilder();

        try {
            Document doc = (Document)builder.build(file);
            FileSystem.getInstance().xmlImport(doc);
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

}
